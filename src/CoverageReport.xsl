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
            <script type="text/javascript" src="http://www.google.com/jsapi"></script>
            <script type="text/javascript">
                google.load("visualization", "1", {packages:["corechart"]});
                google.setOnLoadCallback(function() {
                    $(document).ready(function() {
                
                        // set up read count histograms
                        $(".readHistogram").each(function() {
                            $(this).css("background-position", "0px " + ((1.0 * (100 - parseInt($(this).attr("data-pct")))) / 100) * $(this).outerHeight() + "px");
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
                                    $("#blocker").hide();
                                }, 10);
                            }
                            else {
                                $(this).html("+");
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
                
                    });
                });
            </script>
            <style>
                body { font-family: Arial; }
                table { border-spacing: 0px; border-collapse: collapse; }
                table td, table th { border: 1px solid black; padding: 3px; }
                table tr td, table tr th {
                    page-break-inside: avoid;
                }
                ul { padding-left: 15px; }
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
                }
                #blocker div {
                    position: absolute;
                    top: 10px;
                    left: 10px;
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
            <h3>gVCF file: <xsl:value-of select="/vcf/@fileName"/></h3>
            <h3>exon BED file: <xsl:value-of select="/vcf/@exonBedFileName"/></h3>
            <h3>amplicon BED file: <xsl:value-of select="/vcf/@ampliconBedFileName"/></h3>
            <h3>report run date: <xsl:value-of select="substring(/vcf/@runDate, 1, 16)"/></h3>

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
            </ul>
            
            <table>
                <tr>
                    <th></th>
                    <th colspan="5">gene/exon</th>
                    <th colspan="{count(/vcf/geneExons/geneExon[1]/bins/bin)}">base count by read depth</th>
                </tr>
                <tr>
                    <th>
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
                        <td>
                            <xsl:value-of select="@name"/><br/>
                            <span style="font-size: x-small">
                                Ensembl ID:&#160;<a href="http://www.ensembl.org/id/{@ensemblTranscriptId}"><xsl:value-of select="@ensemblExonId"/></a><br/>
                                vendor ID:&#160;<xsl:value-of select="@vendorGeneExonName"/>
                            </span>
                        </td>
                        <td style="text-align: right;"><xsl:value-of select="format-number(@pctOfExon, '##0')"/></td>
                        <td>
                            <a href="http://localhost:60151/load?file={../../@bedBamVcfFileUrlsAsString}&amp;locus={@chr}:{@startPos}-{@endPos}&amp;genome=hg19&amp;merge=false">
                                <xsl:value-of select="@chr"/>:<xsl:value-of select="@startPos"/>-<xsl:value-of select="@endPos"/>
                            </a><br/>
                        </td>
                        <td><xsl:value-of select="@variantCalled"/></td>
                        <xsl:for-each select="bins/bin">
                            <td class="readHistogram" style="text-align: right; width: 40px; background: url('http://www.bbtm-academy.org/blue.jpg'); background-repeat:no-repeat;" data-pct="{@pct}"><xsl:value-of select="@count"/></td>
                        </xsl:for-each>
                    </tr>
                    <tr style="display: none;" class="geneExon_child geneExon{position()}_child">
                        <td colspan="{count(bins/bin) + 6}">
                            <div id="geneExon{position()}_div"></div>
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
                                            a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:'-' + (svgHeight - 50), width:'1', height:(svgHeight - 50 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                            a(g, 'rect', { x:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/> - 1), y:'-' + (svgHeight - 50), width:'1', height:(svgHeight - 50 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                            a(g, 'text', { x:(((amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>) * xScale) + 5), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                        }
                                        else if((amplicons[x].startPos &lt; <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos &lt;= <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                            a(g, 'rect', { x:'0', y:y, width:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/>), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                            a(g, 'rect', { x:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/> - 1), y:'-' + (svgHeight - 50), width:'1', height:(svgHeight - 50 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                            a(g, 'polygon', { points:'0,' + y + ' 0,' + (y + 25) + ' -25,' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                            a(g, 'text', { x:'-75', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (<xsl:value-of select="bases/base[1]/@pos"/> - amplicons[x].startPos) + " bp";
                                            a(g, 'text', { x:'5', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                        }
                                        else if((amplicons[x].startPos >= <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos > <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                            a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:y, width:(<xsl:value-of select="bases/base[last()]/@pos"/> - amplicons[x].startPos), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                            a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:'-' + (svgHeight - 50), width:'1', height:(svgHeight - 50 + y), opacity:'0.5', style:'fill: ' + color + ';' });
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
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
            
        </body>
        
        <p>Copyright &#169; 2014 Geoffrey H. Smith (geoffrey.hughes.smith@gmail.com)</p>
        
    </html>

</xsl:template>

</xsl:stylesheet>