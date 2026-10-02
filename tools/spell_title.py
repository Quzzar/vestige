"""Display titles only: source spell names remain untouched in provenance."""
import re
MINOR={'a','an','and','as','at','but','by','for','from','in','into','nor','of','on','or','per','the','to','via','with'}
def spell_title(name):
    matches=list(re.finditer(r"[A-Za-z][A-Za-z’']*",name))
    def format_word(match):
        word=match.group();index=next(i for i,m in enumerate(matches) if m.start()==match.start())
        if index not in (0,len(matches)-1) and word.lower() in MINOR:return word.lower()
        if word.isupper() and len(word)<=3:return word
        return word[0].upper()+word[1:].lower()
    return re.sub(r"[A-Za-z][A-Za-z’']*",format_word,name)
