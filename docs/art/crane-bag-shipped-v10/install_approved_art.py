"""Apply only the two approved Crane Bag resources to the existing test-pack JAR."""
from datetime import datetime, timezone
from pathlib import Path
import argparse
import hashlib
import json
import os
import shutil
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
TARGET = Path('/Users/quzzar/Library/Application Support/PrismLauncher/instances/Kithkyn Testing/minecraft/mods/vestige-0.1.0.jar')
BUILT = PROJECT / 'run/crane-bag-shipped-build/libs/vestige-0.1.0.jar'
STAGE = PROJECT / 'run/crane-bag-shipped-install'
ASSETS = ('assets/vestige/models/item/crane_bag.json', 'assets/vestige/textures/item/crane_bag.png')
APPROVED_SHA = '2c8eeebdd4bf6c555d829ff99d0d9694da7f82e3254db7aa42a16628349212e8'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--install', action='store_true')
    args = parser.parse_args()
    verification = json.loads((ROOT / 'verification.json').read_text())
    assert all(value == 'PASS' for value in verification['checks'].values())
    STAGE.mkdir(parents=True, exist_ok=True)
    base = TARGET.read_bytes()
    base_sha = sha(base)
    base_path = STAGE / ('base-' + base_sha + '.jar')
    base_path.write_bytes(base)
    replacements = {name: (PROJECT / 'src/main/resources' / name).read_bytes() for name in ASSETS}
    assert sha(replacements[ASSETS[1]]) == APPROVED_SHA
    with zipfile.ZipFile(BUILT) as built:
        for name, data in replacements.items():
            assert built.read(name) == data, name
    candidate = STAGE / 'vestige-0.1.0.jar'
    with zipfile.ZipFile(base_path) as source, zipfile.ZipFile(candidate, 'w') as result:
        names = source.namelist()
        assert len(names) == len(set(names)), 'Duplicate members in the installed JAR'
        assert 'com/quzzar/vestige/storage/CraneBagItem.class' in names
        assert not any(name.startswith('META-INF/') and name.upper().endswith(('.SF', '.RSA', '.DSA')) for name in names)
        result.comment = source.comment
        for info in source.infolist():
            result.writestr(info, replacements.get(info.filename, source.read(info.filename)))
        for name in ASSETS:
            if name not in names:
                result.writestr(name, replacements[name], compress_type=zipfile.ZIP_DEFLATED)
    with zipfile.ZipFile(base_path) as source, zipfile.ZipFile(candidate) as result:
        assert result.testzip() is None
        assert set(result.namelist()) == set(source.namelist()) | set(ASSETS)
        changed = [name for name in source.namelist() if source.read(name) != result.read(name)]
        added = sorted(set(result.namelist()) - set(source.namelist()))
        assert set(changed + added) <= set(ASSETS)
        for name, data in replacements.items():
            assert result.read(name) == data
        preserved = len(source.namelist()) - len(changed)
    candidate_sha = sha(candidate.read_bytes())
    record = {
        'preparedAtUTC': datetime.now(timezone.utc).isoformat(),
        'instance': 'Kithkyn Testing', 'installedJar': str(TARGET),
        'baseJar': str(base_path), 'baseSha256': base_sha,
        'stagedJar': str(candidate), 'stagedSha256': candidate_sha,
        'changedMembers': changed, 'addedMembers': added,
        'unchangedMembers': preserved, 'textureSha256': APPROVED_SHA,
        'scope': 'Only approved Crane Bag model and PNG; all other installed ZIP member bytes retained.',
        'restartPerformed': False, 'installed': False,
    }
    if args.install:
        assert sha(TARGET.read_bytes()) == base_sha, 'Installed JAR changed; rerun to use the new base'
        stamp = datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%S%fZ')
        backup = TARGET.parent.parent / 'mod-backups' / ('crane-bag-gathered-' + stamp) / TARGET.name
        backup.parent.mkdir(parents=True)
        shutil.copy2(TARGET, backup)
        assert sha(backup.read_bytes()) == base_sha
        descriptor, temporary = tempfile.mkstemp(prefix='.vestige-crane-bag-', suffix='.tmp', dir=TARGET.parent)
        try:
            with os.fdopen(descriptor, 'wb') as output:
                output.write(candidate.read_bytes())
                output.flush()
                os.fsync(output.fileno())
            shutil.copymode(TARGET, temporary)
            assert sha(TARGET.read_bytes()) == base_sha, 'Installed JAR changed before replacement'
            os.replace(temporary, TARGET)
        finally:
            if Path(temporary).exists():
                Path(temporary).unlink()
        assert sha(TARGET.read_bytes()) == candidate_sha
        record.update(installed=True, installedAtUTC=datetime.now(timezone.utc).isoformat(),
                      installedSha256=candidate_sha, backupJar=str(backup), backupSha256=base_sha,
                      atomicReplacement=True)
    (ROOT / ('prism-install.json' if args.install else 'staged-install.json')).write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps(record, indent=2))


if __name__ == '__main__':
    main()
