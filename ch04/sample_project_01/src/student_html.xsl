<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <xsl:template match="/">
  <html>
    <head>
	  <title>Student Directory</title>
	</head>
	<body>
    <xsl:apply-templates/>
	</body>
  </html>
  </xsl:template>

  <xsl:template match="student_list">
    <h3>Student Directory for example.edu</h3>
	<xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="name">
    <p>Name: <xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="major">
    <p>Major: <xsl:apply-templates/></p>
  </xsl:template>
  
  <xsl:template match="phone">
    <p>Phone: <xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="email">
    <p>Email: <xsl:apply-templates/></p>
  </xsl:template>
</xsl:stylesheet>
