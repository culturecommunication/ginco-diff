<?xml version="1.0" encoding="utf-8"?>
<!-- 
This software is governed by the CeCILL-B license under French law and
abiding by the rules of distribution of free software. You can use,
modify and/or redistribute the software under the terms of the CeCILL-B
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty and the software's author, the holder of the
economic rights, and the successive licensors have only limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading, using, modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean that it is complicated to manipulate, and that also
therefore means that it is reserved for developers and experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and, more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-B license and that you accept its terms.
-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:srx="http://www.w3.org/2005/sparql-results#">

  <xsl:output method="xml" version="1.0" indent="yes"/>

  <xsl:param name="baseUri"/>
  <xsl:param name="baseUrl"/>

  <!-- Translate resource URIs in SPARQL Results documents. -->
  <xsl:template match="/srx:sparql/srx:results/srx:result/srx:binding/srx:uri/text()">
    <xsl:choose>
      <xsl:when test="starts-with(.,$baseUri)">
        <xsl:value-of select="concat($baseUrl, substring-after(.,$baseUri))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Translate resource URIs in RDF documents. -->
  <xsl:template match="@rdf:about|@rdf:resource">
    <xsl:attribute name="{name()}" namespace="{namespace-uri()}">
      <xsl:choose>
        <xsl:when test="starts-with(.,$baseUri)">
          <xsl:value-of select="concat($baseUrl, substring-after(.,$baseUri))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <!-- Render all other nodes verbatim. -->
  <xsl:template match="*|@*|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
