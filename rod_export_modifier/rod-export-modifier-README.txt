=================================================================================
README aplikace ${project.name} verze ${project.version}
=================================================================================

-------------------------------------------
----Vstupní parametry:
-------------------------------------------
-dir <dir>		- adresář ve kterém se to má provést (hleda se 'index.html') a kam se uloží výsledek (jedinny povinny parametr).
-css <path>		- jmeno (cesta k) souboru se styly, ktere se maji v index.html nahradit, default 'styles.css'
-changes <path>	- jmeno (cesta k) souboru se zmenami, ktere se maji do index.html pridat, default 'changes.txt'
-xhtml			- flag, zda se ma zapsat na disk html zkonvertovane do xhtml (kdyz neni pouzito, tak false, kdyz je, tak true)
-nc				- flag (no compress), ze se nema na zaver vysledek zagzipovat (kdyz se nepouzije, vysledek se gzipuje)

-------------------------------------------
----Priklady pouziti aplikace:
-------------------------------------------
remo -dir html_2014.04.08
remo -dir .
remo -xhtml -dir .
remo -dir html_2014.04.08 -css styly.css -changes zmeny.txt

-------------------------------------------
----Popis:
-------------------------------------------
Aplikace najde ve zvolenem adresari soubor 'index.html' a provede v nem transformace.
Po nastaveni adresare do PATH lze poustet odkudkoliv. Aplikace loguje do konzole.

-------------------------------------------
----Prime spusteni .jar souboru z adresare "${project.artifactId}-${project.version}":
-------------------------------------------
java -jar ${project.artifactId}-${project.version}.jar