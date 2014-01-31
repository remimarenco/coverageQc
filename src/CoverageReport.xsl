<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>

<xsl:template match="/vcf">

    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html>
    </xsl:text>
    
    <html>

        <xsl:text disable-output-escaping="yes">
            &lt;!--
                author: geoffrey.hughes.smith@gmail.com
            -->
        </xsl:text>
            
        <head>
            <title>Coverage QC Report</title>
            <script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
            <script type="text/javascript" src="http://code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
            <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css"/>
            <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.13.3/jquery.tablesorter.min.js"></script>
            <link rel="stylesheet" type="text/css" href="http://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.13.3/css/theme.default.css"/>
            <script type="text/javascript" src="http://www.google.com/jsapi"></script>
            <script type="text/javascript">
                google.load("visualization", "1", {packages:["corechart"]});
                google.setOnLoadCallback(function() {
                    $(document).ready(function() {
                
                        // set up read count histograms
                        $(".readHistogram").each(function() {
                            $(this).css("background-position", "0px " + ((1.0 * (100 - parseInt($(this).attr("data-pct")))) / 100) * $(this).outerHeight() + "px");
                        });

                        // engage the tablesorter
                        $("#qcReportTable").tablesorter({
                            headers: {
                                0: { sorter:false },
                                1: { sorter:false },
                                2: { sorter:false },
                                3: { sorter:false },
                                4: { sorter:"text" },
                                5: { sorter:"text" },
                                6: { sorter:"text" },
                                7: { sorter:"text" },
                                8: { sorter:"text" },
                                9: { sorter:"text" },
                                10: { sorter:"text" },
                                11: { sorter:"text" },
                            }
                        });
                                
                        // expand all takes so long that a modal "wait..." is displayed
                        $("#geneExonExpandCollapseAllButton").bind("click", function() {
                            if($(this).html() == "+") {
                                $(this).html("-");
                                $("#blocker").show();
                                setTimeout(function() {
                                    var geneExonPos = 1;
                                    $(".geneExon_child").each(function() {
                                        $(this).show();
                                        eval("geneExon" + geneExonPos + "_drawChart()");
                                        geneExonPos++;
                                    });
                                    $(".geneExonExpandCollapseButton").html("-");
                                    $("#blocker").hide();
                                }, 10);
                            }
                            else {
                                $(this).html("+");
                                $(".geneExonExpandCollapseButton").html("+");
                                $(".geneExon_child").hide();
                            }
                            return false;
                        });
                
                        $(".geneExonExpandCollapseButton").bind("click", function() {
                            $("." + $(this).attr('id') + "_child").toggle();
                            if($(this).html() == "+") {
                                $(this).html("-");
                                eval($(this).attr('id') + "_drawChart()");
                            }
                            else {
                                $(this).html("+");
                            }
                            return false;
                
                        });
                
                        $("#exportLink").bind("click", function() {
                            showExportDialog();
                            return false;
                        });
                
                    });
                });
            </script>
            <style>
                
                body { 
                    font-family: Arial; 
                }

                ul {
                    padding-left: 20px;
                }

                .dataTable {
                    width: auto;
                    font: inherit;
                    border-collapse: collapse;
                    border-spacing: 0px;
                    margin-left: 0px;
                }

                .dataTable th {
                    font: inherit;
                    border: 1px solid black !important;
                    padding: 4px;
                    vertical-align: middle; 
                }

                .dataTable td {
                    border: 1px solid black;
                    padding: 4px;
                    vertical-align: middle; 
                }

                .dataTable tr th, table tr td {
                    page-break-inside: avoid;
                }
                
                #blocker {
                    display: none;
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    opacity: .7;
                    background-color: black;
                    z-index: 1000;
                    text-align: center;
                    vertical-align: middle;
                }
                
                #blocker div {
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    color: white;
                    font-size: x-large;
                    font-weight: bold;
                }
                
            </style>
        </head>
        
        <body>

            <div id="blocker">
                <div>wait...</div>
            </div>
            
            <h1>Coverage QC Report</h1>
            <table>
                <tr><td>report run date</td><td>:</td><td style="font-weight: bold;"><xsl:value-of select="substring(/vcf/@runDate, 1, 16)"/></td></tr>
                <tr><td>gVCF file</td><td>:</td><td style="font-weight: bold;"><xsl:value-of select="/vcf/@fileName"/></td></tr>
                <tr><td>variant TSV file</td><td>:</td><td style="font-weight: bold;"><xsl:value-of select="/vcf/@variantTsvFileName"/></td></tr>
                <tr><td>exon BED file</td><td>:</td><td style="font-weight: bold;"><xsl:value-of select="/vcf/@exonBedFileName"/></td></tr>
                <tr><td>amplicon BED file</td><td>:</td><td style="font-weight: bold;"><xsl:value-of select="/vcf/@ampliconBedFileName"/></td></tr>
            </table>

            <ul>
                <li>Base positions start at zero (0).</li>
                <li>QC rules are applied to bases <i>in the coding region</i> of each locus:
                    <ul>
                        <li>pass: <i>all</i> bases read <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[4]/@name" disable-output-escaping="yes"/> times</li>
                        <li>warn: <i>all</i> bases read <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[3]/@name" disable-output-escaping="yes"/> or <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[4]/@name" disable-output-escaping="yes"/> times</li>
                        <li>fail: <i>any</i> base read <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[1]/@name" disable-output-escaping="yes"/> or <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[2]/@name" disable-output-escaping="yes"/> times</li>
                    </ul>
                </li>
                <li>Coding regions and amplicons are specified by vendor.</li>
                <li>If the gVCF file contains multiple entries for the same position (e.g., indels), the maximum read depth value is reported here.</li>
                <xsl:if test="count(*/*/variants/variant) > 0">
                    <li>After selecting variants for export, <a id="exportLink" href="#">click here</a> to see them as a text document suitable for cut-and-paste operations.</li>
                </xsl:if>
            </ul>
            
            <table id="qcReportTable" class="dataTable">
                <thead>
                    <tr>
                        <th></th>
                        <th colspan="5">gene/exon</th>
                        <th colspan="{count(/vcf/geneExons/geneExon[1]/bins/bin)}">base count by read depth</th>
                    </tr>
                    <tr>
                        <th style="text-align: center;">
                            <a href="#" id="geneExonExpandCollapseAllButton" style="color: blue; text-decoration: none; font-size: large; font-weight: bold;">+</a>
                        </th>
                        <th>QC</th>
                        <th>name</th>
                        <th>% exon<br/>reported</th>
                        <th>locus</th>
                        <th>variant</th>
                        <xsl:for-each select="/vcf/geneExons/geneExon[1]/bins/bin">
                            <th><xsl:value-of select="@name" disable-output-escaping="yes"/> <br/>reads</th>
                        </xsl:for-each>
                    </tr>
                </thead>
                <xsl:for-each select="/vcf/geneExons/geneExon">
                    <xsl:variable name="weight">
                        <xsl:choose>
                            <xsl:when test="@variantCalled = 'true'">bold</xsl:when>
                            <xsl:otherwise>normal</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="color">
                        <xsl:choose>
                            <xsl:when test="@qc = 'fail'">red</xsl:when>
                            <xsl:when test="@qc = 'warn'">yellow</xsl:when>
                            <xsl:when test="@qc = 'pass'">green</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <tr style="font-weight: {$weight};">
                        <td style="text-align: center; width: 20px;">
                            <a href="#" id="geneExon{position()}" class="geneExonExpandCollapseButton" style="color: blue; text-decoration: none; font-size: large; font-weight: bold;">+</a>
                        </td>
                        <td style="background-color: {$color};"><xsl:value-of select="@qc"/></td>
                        <td data-export-label="exon">
                            <xsl:value-of select="@name"/><br/>
                            <span style="font-size: x-small">
                                Ensembl ID: <a href="http://www.ensembl.org/id/{@ensemblTranscriptId}"><xsl:value-of select="@ensemblExonId"/></a><br/>
                                vendor ID: <xsl:value-of select="@vendorGeneExonName"/>
                            </span>
                        </td>
                        <td style="text-align: right;"><xsl:value-of select="format-number(@pctOfExon, '##0')"/></td>
                        <td data-export-label="locus">
                            <a href="http://localhost:60151/load?file={../../@bedBamVcfFileUrlsAsString}&amp;locus={@chr}:{@startPos}-{@endPos}&amp;genome=hg19&amp;merge=false">
                                <xsl:value-of select="@chr"/>:<xsl:value-of select="@startPos"/>-<xsl:value-of select="@endPos"/>
                            </a>
                        </td>
                        <td><xsl:value-of select="@variantCalled"/></td>
                        <xsl:for-each select="bins/bin">
                            <td class="readHistogram" style="text-align: right; width: 40px; background: url('http://www.bbtm-academy.org/gray.jpg'); background-repeat:no-repeat;" data-pct="{@pct}"><xsl:value-of select="@count"/></td>
                        </xsl:for-each>
                    </tr>
                    <tr style="display: none;" class="tablesorter-childRow geneExon_child geneExon{position()}_child">
                        <td colspan="{count(bins/bin) + 6}" style="background: white;">
                            <div id="geneExon{position()}_div">
                                <script type="text/javascript">

                                    function geneExon<xsl:value-of select="position()"/>_drawChart() {

                                        function a(parentElement, element, eldict) { 
                                           el = $(document.createElementNS('http://www.w3.org/2000/svg', element));
                                           el.attr(eldict).appendTo(parentElement);
                                           return el;
                                        }

                                        // don't draw the chart if it has already been drawn
                                        if($("#geneExon<xsl:value-of select="position()"/>_div svg").length > 0) {
                                            return;
                                        }

                                        // draw chart using Google Charts
                                        var data = {
                                        cols:[
                                        {id:'pos', label:'pos', type:'number'}
                                        ,{type:'string', role:'annotation'}
                                        ,{type:'string', role:'annotationText'}
                                        ,{id:'readDepth', label:'reads', type:'number'}
                                        ,{id:'qcThreshold', label:'QC level', type:'number'}
                                        ]
                                        ,rows:[
                                        <xsl:for-each select="bases/base">
                                            <xsl:if test="position() != 1">,</xsl:if>{c:[{v:<xsl:value-of select="@pos"/>}, {v:'<xsl:value-of select="@variant"/>'}, {v:'<xsl:value-of select="@variantText"/>'}, {v:<xsl:value-of select="@totalReadDepth"/>}, {v:<xsl:value-of select="../../bins/bin[last()]/@startCount"/>}]}
                                        </xsl:for-each>
                                        ]};
                                        var dataTable = new google.visualization.DataTable(data);
                                        var dataView = new google.visualization.DataView(dataTable);
                                        var chart = new google.visualization.LineChart(document.getElementById('geneExon<xsl:value-of select="position()"/>_div'));
                                        chart.draw(dataView, { colors: ['blue', 'red'], annotations: { style: 'line' } });

                                        // add the amplicon guides to chart (custom SVG)
                                        var amplicons = [
                                        <xsl:for-each select="amplicons/amplicon">
                                            <xsl:if test="position() != 1">,</xsl:if>{name:'<xsl:value-of select="@name"/>', startPos:<xsl:value-of select="@startPos"/>, endPos:'<xsl:value-of select="@endPos"/>'}
                                        </xsl:for-each>
                                        ];
                                        var svg = $('#geneExon<xsl:value-of select="position()"/>_div svg');
                                        var svgHeight = parseInt(svg.css('height')) + (30 * amplicons.length);
                                        var chartBoundingBox = chart.getChartLayoutInterface().getChartAreaBoundingBox();
                                        var xScale =  (1.0 * chartBoundingBox.width) / (1.0 * (<xsl:value-of select="bases/base[last()]/@pos"/> - <xsl:value-of select="bases/base[1]/@pos"/>));
                                        $('#geneExon<xsl:value-of select="position()"/>_div >:first-child').css({ height:svgHeight + 'px' });
                                        $('#geneExon<xsl:value-of select="position()"/>_div svg').css({ height:svgHeight + 'px' });
                                        var g = a(svg, 'g', { class:'amplicons', transform:'translate(' + chartBoundingBox.left + ' ' + (svgHeight - (30 * amplicons.length) - 10) + ') scale(' + xScale + ' 1)' });
                                        for(var x = 0; x &lt; amplicons.length; x++) {
                                            var y = 30 * x;
                                            var color
                                            if(amplicons[x].name.match(/^.*_coding$/) != null) {
                                                color = 'green';
                                            }
                                            else {
                                                color = 'gray';
                                            }
                                            if((amplicons[x].startPos >= <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos &lt;= <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                                a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:y, width:(amplicons[x].endPos - amplicons[x].startPos), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'rect', { x:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/> - 1), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'text', { x:(((amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>) * xScale) + 5), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                            }
                                            else if((amplicons[x].startPos &lt; <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos &lt;= <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                                a(g, 'rect', { x:'0', y:y, width:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/>), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'rect', { x:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/> - 1), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'polygon', { points:'0,' + y + ' 0,' + (y + 25) + ' -25,' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'text', { x:'-75', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (<xsl:value-of select="bases/base[1]/@pos"/> - amplicons[x].startPos) + " bp";
                                                a(g, 'text', { x:'5', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                            }
                                            else if((amplicons[x].startPos >= <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos > <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                                a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:y, width:(<xsl:value-of select="bases/base[last()]/@pos"/> - amplicons[x].startPos), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'polygon', { points:chartBoundingBox.width + ',' + y + ' ' + chartBoundingBox.width + ',' + (y + 25) + ' ' + (chartBoundingBox.width + 25) + ',' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'text', { x:(chartBoundingBox.width + 27), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (amplicons[x].endPos - <xsl:value-of select="bases/base[last()]/@pos"/>) + " bp";
                                                a(g, 'text', { x:(((amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>) * xScale) + 5), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                            }
                                            else if((amplicons[x].startPos &lt; <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos > <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                                a(g, 'rect', { x:'0', y:y, width:(<xsl:value-of select="bases/base[last()]/@pos"/> - <xsl:value-of select="bases/base[1]/@pos"/>), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'polygon', { points:'0,' + y + ' 0,' + (y + 25) + ' -25,' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'text', { x:'-75', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (<xsl:value-of select="bases/base[1]/@pos"/> - amplicons[x].startPos) + " bp";
                                                a(g, 'polygon', { points:chartBoundingBox.width + ',' + y + ' ' + chartBoundingBox.width + ',' + (y + 25) + ' ' + (chartBoundingBox.width + 25) + ',' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                                a(g, 'text', { x:(chartBoundingBox.width + 27), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (amplicons[x].endPos - <xsl:value-of select="bases/base[last()]/@pos"/>) + " bp";
                                                a(g, 'text', { x:'5', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                            }
                                        }

                                    }

                                </script>
                            </div>
                            <xsl:if test="count(variants/variant) > 0">
                                <div style="font-size: small; padding: 10px;">
                                    <h3>Filtered and Annotated Variant(s)</h3>
                                    <table class="dataTable">
                                        <tr>
                                            <th>export?</th>
                                            <th>gene</th>
                                            <th>coordinate</th>
                                            <th>consequence</th>
                                            <th>genotype</th>
                                            <th>AVF</th>
                                            <th>cDNA</th>
                                            <th>amino acid</th>
                                            <th>dbSNP</th>
                                            <th>MAF</th>
                                            <th>COSMIC ID</th>
                                        </tr>
                                        <xsl:for-each select="variants/variant">
                                            <tr>
                                                <td style="text-align: center;"><input type="checkbox" class="exportCheckbox"/></td>
                                                <td data-export-label="gene"><xsl:value-of select="@gene"/></td>
                                                <td data-export-label="coordinate">chr<xsl:value-of select="@chr"/>:<xsl:value-of select="@coordinate"/></td>
                                                <td data-export-label="consequence"><xsl:value-of select="@consequence"/></td>
                                                <td data-export-label="genotype"><xsl:value-of select="@genotype"/></td>
                                                <td data-export-label="avf" style="text-align: right;"><xsl:value-of select="@altVariantFreq"/></td>
                                                <td data-export-label="cDna"><xsl:value-of select="@hgvsc"/></td>
                                                <td data-export-label="aminoAcid"><xsl:value-of select="@hgvsp"/></td>
                                                <td><a href="http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?{@dbSnpIdPrefix}={@dbSnpIdSuffix}"><xsl:value-of select="@dbSnpIdPrefix"/><xsl:value-of select="@dbSnpIdSuffix"/></a></td>
                                                <td data-export-label="maf" style="text-align: right;"><xsl:value-of select="@alleleFreqGlobalMinor"/></td>
                                                <td><a href="http://cancer.sanger.ac.uk/cosmic/search?q={@cosmicId}"><xsl:value-of select="@cosmicId"/></a></td>
                                            </tr>
                                        </xsl:for-each>
                                    </table>
                                </div>
                            </xsl:if>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>

            <p>Copyright &#169; 2014 Geoffrey H. Smith (geoffrey.hughes.smith@gmail.com)</p>
            
            <xsl:if test="count(*/*/variants/variant) > 0">
                <div id="exportDialog" style="display: none; font-family: Courrier;" title="export selected variant(s)">
                    <script type="text/javascript">

                        function showExportDialog() {
                            var exportMap = new Object();
                            var exportText = "";
                            $(".exportCheckbox:checked").each(function() {
                                $(this).parents("tr").prev("tr").find("td[data-export-label]").each(function() {
                                    if($(this).attr("data-export-label") == "exon") {
                                        var re = /(.*)[\s\n]*Ensembl ID: (.*)[\s\n]*?vendor ID: (.*)/gm;
                                        var match = re.exec($(this).text());
                                        exportMap["exonName"] = match[1];
                                        exportMap["exonEnsemblId"] = match[2];
                                        exportMap["exonVendorId"] = match[3];
                                    }
                                    else {
                                        exportMap[$(this).attr("data-export-label")] = $(this).text();
                                    }
                                });
                                $(this).parents("tr").find("td[data-export-label]").each(function() {
                                    exportMap[$(this).attr("data-export-label")] = $(this).text();
                                });
                                exportText += "A(n) " + exportMap.gene + " " + exportMap.cDna + " / " + exportMap.aminoAcid + " variant was detected by next generation sequencing";
                                exportText += " in exon " + exportMap.exonName + " (Ensembl ID: " + exportMap.exonEnsemblId + " / vendor ID: " + exportMap.exonVendorId + " / " + exportMap.locus + ").\n\n";
                                exportText += "coord (base 0)\tconsequence\t\tgenotype\talt-variant-freq\tminor-allele-freq\n";
                                exportText += "--------------\t-----------\t\t--------\t----------------\t-----------------\n";
                                exportText += exportMap.coordinate + "\t" + exportMap.consequence + "\t" + exportMap.genotype + "\t\t" + exportMap.avf + "\t\t\t" + exportMap.maf + "\n";
                                exportText += "\n\n";
                            });
                            $("#exportDialog").html(exportText.length > 0 ? "&lt;textarea style='width: 95%; height: 95%; font-family: Courrier;'>" + exportText + "&lt;/textarea>" : "no variants selected for export");
                            $("#exportDialog").dialog({
                                width:$(window).width() * 0.6,
                                height:$(window).height() * 0.6
                            });
                        }

                    </script>
                </div>
            </xsl:if>

        </body>
        
    </html>

</xsl:template>

</xsl:stylesheet>