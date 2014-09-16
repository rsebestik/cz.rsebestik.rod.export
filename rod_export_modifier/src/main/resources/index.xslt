<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='2.0'
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:a="http://www.w3.org/1999/xhtml"
		exclude-result-prefixes="a">

	<xsl:output method='xhtml' />
	
	<xsl:param name="cssContent" />
	<xsl:param name="jsContent" />
	<xsl:param name="jsLabelsVar"/><!-- obsahuje JS definici mapy pro nahraznovani zkratek labelu -->
	<xsl:param name="jsTitlesVar"/><!-- obsahuje JS definici mapy pro nahraznovani zkratek labelu -->
	<xsl:param name="jsVztahyVar"/>
	<xsl:param name="changesContent" />
	<xsl:param name="lastChangeDate" />
	<xsl:param name="jqueryFileName" /><!-- filename of jquery JS file - contains merged all jquery file contents -->
	<xsl:param name="personDetails"/>
	<xsl:param name="subdirSource"/>
	<xsl:param name="subdirResult"/><!-- cilovy adresar s obrazky (napr: 'img') -->

<!-- <xsl:value-of select="system-property('xsl:version')" />!!<xsl:value-of select="system-property('xsl:vendor')" /> -->
	<xsl:variable name="rodImg" select="concat($subdirSource, '/rodokmen.png')"/>

	<!-- copy all -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<!-- replace title with title and current date -->
	<xsl:template match="/a:html/a:head/a:title">
		<title>Rodokmen (<xsl:value-of select="$lastChangeDate"/>)</title>
	</xsl:template>
	
	<!-- new line var -->
	<xsl:variable name="nl" select="'&#xA;'"/>

	<!-- replace styles with content from param -->
	<xsl:template match="/a:html/a:head/a:style">
		<link href="jquery/jquery-ui-1.10.4.custom.css" rel="stylesheet"/>
		<xsl:value-of select="$nl"/>
		<link href="jquery/jquery.fancybox.css" rel="stylesheet"/>
		<xsl:value-of select="$nl"/>
		<style type="text/css">
			<xsl:value-of select='concat($nl,normalize-space($cssContent),$nl)' disable-output-escaping="yes"/>
		</style>
	</xsl:template>

	<!-- replace <script> block with modified JS functions, added jquery and also with function that shows changelog -->
	<xsl:template match="/a:html/a:head/a:script">
		<xsl:element name="script">
			<xsl:attribute name="src" select="$jqueryFileName"/>
			<xsl:attribute name="type">text/javascript</xsl:attribute>
		</xsl:element>
		<xsl:value-of select="$nl"/>
		<script type="text/javascript">
			<xsl:variable name="subdirJsVar" select="concat('var subdir=''', $subdirResult, '/'';')"/>
			<xsl:value-of select='concat($nl,$subdirJsVar,$nl,$jsLabelsVar,$nl,$jsTitlesVar,$nl,$jsVztahyVar,$nl,normalize-space($jsContent),$nl)' disable-output-escaping="yes"/>
		</script>
	</xsl:template>
	
	<!-- modify onload on <body> (center rodokmen image, show changelog (and remove useless onresize function)) -->
	<xsl:template match="/a:html/a:body/@*">
		<xsl:attribute name="onload">imgLoad(<xsl:value-of select="//a:img[@src=$rodImg]/@width"/>, <xsl:value-of select="//a:img[@src=$rodImg]/@height"/>);odznac();$('#rodSplitter').trigger('resize');showChanges()</xsl:attribute>
	</xsl:template>

	<!-- change document structure -->
	<xsl:template match="//a:div[@id='doc']">
		<div id="doc">
			<div id="rodSplitter">
				<div id="left">
		            <table id="personDetails"></table>
				</div>
				<div id="right">
					<!-- zkopirovani puvodniho obsahu 'pri-main' coz je sekce s rodokmenem a mapou osob -->
					<xsl:apply-templates select="//a:div[@id='pri-main']"/>
				</div>
			</div>
		</div>
		<xsl:element name="div">
			<xsl:attribute name="id">details</xsl:attribute>
			<xsl:attribute name="style">display:none;</xsl:attribute>
			<xsl:for-each select="$personDetails">
				<xsl:value-of select="." disable-output-escaping="yes"/>
			</xsl:for-each>
			<xsl:element name="div">
				<xsl:attribute name="id">lastChanges</xsl:attribute>
				<xsl:attribute name="title">Poslední změny</xsl:attribute>
				<xsl:value-of select="$changesContent" disable-output-escaping="yes"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="//a:img[@src=$rodImg]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="onclick" select="lower-case(@onclick)"/>
			<xsl:attribute name="id" select="'rodImg'"/>
		</xsl:copy>
	</xsl:template>
	<!-- delete onload attribute on rodokmen img -->
	<xsl:template match="//a:img[@src=$rodImg]/@onload"/>

	<!-- zkrouhni atributy tagu 'area' - vse potrebne se doda az pomoci jQuery (href, onclick, shape) (-> uspora velikosti html souboru) -->
	<xsl:template match="//a:area/@target"/>
	<xsl:template match="//a:area/@href"/>
	<xsl:template match="//a:area/@alt"/>
	<xsl:template match="//a:area/@onclick"/>
	<xsl:template match="//a:area/@shape"/>
	
	<!-- replace subdir name for href and src attributes .. 'img' instead of 'index_soubory' -->
	<xsl:template match="@src|@href">
		<xsl:choose>
			<xsl:when test="starts-with(., $subdirSource)">
				<xsl:variable name="newVal" select="concat($subdirResult, substring-after(., $subdirSource))"/>
				<xsl:if test="name(.) = 'src'">
					<xsl:attribute name="src" select="$newVal"/>
				</xsl:if>
				<xsl:if test="name(.) = 'href'">
					<xsl:attribute name="href" select="$newVal"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="src" select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>