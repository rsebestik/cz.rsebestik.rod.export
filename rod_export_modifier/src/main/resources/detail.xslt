<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='2.0'
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:a="http://www.w3.org/1999/xhtml"
		xmlns:java="http://xml.apache.org/xslt/java"
		exclude-result-prefixes="a">

	<xsl:output method='xhtml' />
	
	<xsl:param name="id" />
	<xsl:param name="lastChangeDate" />
	<xsl:param name="labelTexts" />
	<xsl:param name="labelShortcuts" />
	<xsl:param name="titleTexts" />
	<xsl:param name="titleShortcuts" />
	
	<!-- copy all -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<!-- leave in page only table with ID of the person -->
	<xsl:template match="/a:html">
		<xsl:element name="table">
			<xsl:attribute name="id" select="$id"/>
			<!-- pridani data posledni zmeny na zacatek tabulky default stranky (info o rodokmenu) -->
			<xsl:if test="$id = 'vlastRodok'">
				<tr>
					<td colspan="2">
						<div id="nadpisRodokmenu">
							<xsl:variable name="lastChangesText" select="concat('Poslední změny (', $lastChangeDate, ')')"/>
			                <a href="javascript:;" onclick="showChanges()" title="Klikněte pro zobrazeni posledních změn.">
			                	<xsl:value-of select="$lastChangesText"/>
			                </a>
			            </div>
					</td>
				</tr>
			</xsl:if>
			<xsl:apply-templates select="/a:html/a:body/a:table/node()" />
		</xsl:element>
	</xsl:template>
	
	<!-- delete div with link to MAX page -->
	<xsl:template match="/a:html/a:body/a:table/a:tr/a:td/a:div[@style='position: absolute; top: 0px; right: 0px; _right: 15px; padding: 4px 7px 3px 3px;']"/>
	
	<!-- 
		template pro nahrazeni textu za zkratky (funguje rekurzivne)
	 -->
	<xsl:template name="replaceSubstringsWithShortcuts">
		<xsl:param name="text"/><!-- text ve kterem se maji nahradit substringy -->
		<xsl:param name="texts"/><!-- pole textu k nahrazeni -->
		<xsl:param name="shortcuts"/><!-- pole zkratek .. poradi a velikost odpovida parametru 'texts' -->

		<xsl:param name="i" select="1"/><!-- index -->
	
		<xsl:choose>
			<xsl:when test="$i le count($texts)">
				<xsl:variable name="newText" select="replace($text,$texts[$i],$shortcuts[$i])" />
	
				<xsl:call-template name="replaceSubstringsWithShortcuts">
					<xsl:with-param name="i" select="$i+1" />
					<xsl:with-param name="text" select="$newText" />
					<xsl:with-param name="texts" select="$texts"/>
					<xsl:with-param name="shortcuts" select="$shortcuts"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- delete 'onclick' attribute on anchors
		modify 'href' -> it will contain only img name (subdir will be added with JS) 
		find title and replace substrings with shortcuts
	-->
	<xsl:template match="//a:a">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="href" ><xsl:value-of select="@href"/></xsl:attribute>

			<!-- insert title attribute - search for its text in parents TR preceding sibling - and replace substrings with shortcuts-->
			<xsl:variable name="title" select="normalize-space(ancestor::a:tr[1]/preceding-sibling::a:tr[1]/a:td[1]/node())"/>
			<xsl:variable name="title" select="replace($title,'=x ','')"/>
			<xsl:variable name="title">
				<xsl:call-template name="replaceSubstringsWithShortcuts">
					<xsl:with-param name="text" select="$title"/>
					<xsl:with-param name="texts" select="tokenize($titleTexts, ';')"/>
					<xsl:with-param name="shortcuts" select="tokenize($titleShortcuts, ';')"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:attribute name="title" select="$title"/>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>
	<!-- delete useless attributtes -->
	<xsl:template match="//a:a/@alt"/>
	<xsl:template match="//a:a/@onclick"/>
	<!-- <xsl:template match="//a:a/@href"/> -->
	<xsl:template match="//a:a/@target"/>
	
	<!-- KVULI TOMU ABY SE PRI NACITANI STRANKY NETAHALY IHNED VSECHNY mini OBRAZKY,
		tak je 'src' smazano u <img>
		Kdyz pak uzivatel klikne na osobu, tak je v JS zajisteno, ze se do 'src' da u vsech jeho obrazku hodnota 
		z nadrazeneho <a href="..."> pricemz se ale tato hodnota pozmeni na obrazek se sufixem '_mini'
		Tim je zajisteno, ze se obrazky nacitaji az pri kliknuti na osobu.
	 -->
	<xsl:template match="//a:img/@src"/>
	
	<!-- 
		nahrazeni opakujicich se labelu (jako "Rodné příjmení") za zkratky -> aby byl vysledny html soubor vyrazne mensi.
		seznam zkratek a co nahrazuji, je zaroven ulozen jako JS mapa -> a pri zobrazovani osoby v JS jsou tyto zkratky
		pak nahrazeny za puvodni texty. Funguje i pro jednotliva slova v textech.
	-->
	<xsl:variable name="texts" select="tokenize($labelTexts, ';')"/>
	<xsl:variable name="shortcuts" select="tokenize($labelShortcuts, ';')"/>
	<xsl:template match="//a:td/text() | //a:div/text()">
		<xsl:variable name="newText">
			<xsl:choose>
				<xsl:when test="contains(concat(';', $labelTexts, ';'), concat(';', ., ';'))">
					<xsl:variable name="index" select="index-of(($texts), .)"/>
					<xsl:value-of select="$shortcuts[$index]"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="tokenize(., concat('\s+|',codepoints-to-string(160)))">
						<xsl:choose>
							<xsl:when test="contains(concat(';', $labelTexts, ';'), concat(';', ., ';'))">
								<xsl:variable name="index" select="index-of(($texts), .)"/>
								<xsl:sequence select="$shortcuts[$index]"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="."/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:value-of select="normalize-space($newText)"/>
	</xsl:template>

	<!-- delete table rows, that contains image original names (will move it to title text)  -->
	<xsl:template match="//a:tr[a:td[@class='naStredTucne']]"/>
	
	<!-- smazani align="center" -> az pomoci jQuery bude pridan class="c" -->
	<xsl:template match="//a:td[@align='center']/@align"/>
	
	<!-- zkraceni dlouhych classNames -->
	<xsl:template match="//a:tr/@class | //a:td/@class | //a:div/@class">
		<xsl:choose>
			<!-- odd radky nechci odlisovat a levouBunku nastavim az pomoci jQuery na class='l' -->
			<xsl:when test=". = 'odd' or . = 'bunkaLeva'">
				<!-- delete -->
			</xsl:when>
			<xsl:when test=". = 'odsazeniTucne'">
				<xsl:attribute name="class" select="'p'"/>
			</xsl:when>
			<xsl:when test=". = 'nadpis'">
				<xsl:attribute name="class" select="'n'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="//a:img/@alt"/>
	<!-- <xsl:template match="//a:td/@class[.='bunkaLeva']"/> -->
	
	<!-- odstraneni nechteneho TR, ktery obsahuje TD s DIVem se jmenem osoby-->
	<xsl:template match="//a:tr[a:td[a:div[@style='padding: 7px 30px 7px 7px; font-weight: bold; text-align: center;']]]"/>
</xsl:stylesheet>