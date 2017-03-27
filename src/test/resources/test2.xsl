<?xml version="1.0" encoding="UTF-8"?>
<!--
 This Source Code Form is subject to the terms of 
 the Mozilla Public License, v. 2.0. If a copy of 
 the MPL was not distributed with this file, You 
 can obtain one at https://mozilla.org/MPL/2.0/.
-->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:efl-ext="top:marchand:xml:extfunctions"
    version="2.0">
    <xsl:output method="xml"/>
    
    <xsl:template match="/">
        <result>
            <xsl:copy-of select="efl-ext:basex-query('for $i in 1 to 10 return &lt;test&gt;{$i}&lt;/test&gt;', 'localhost', '1984', 'admin', 'admin')"/>
        </result>
    </xsl:template>

</xsl:stylesheet>
