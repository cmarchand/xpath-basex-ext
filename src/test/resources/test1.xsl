<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:efl-ext="fr:efl:saxon:extensions"
    version="2.0">
    <xsl:output method="xml"/>
    
    <xsl:variable name="connect" as="element()"><basex><server>localhost</server><port>1984</port><user>admin</user><password>admin</password></basex></xsl:variable>

    <xsl:template match="/">
        <result>
                <xsl:copy-of select="efl-ext:basex-query('for $i in 1 to 10 return &lt;test&gt;{$i}&lt;/test&gt;',$connect)"/>
        </result>
    </xsl:template>

</xsl:stylesheet>
