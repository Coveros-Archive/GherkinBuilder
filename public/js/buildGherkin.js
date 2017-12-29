function makeDynamic() {
    //make our testSteps sortable
    $( ".testSteps" ).sortable({
        stop: function(event,ui) {
            buildTable( $(ui.item).parent().parent() );
        }
    });
    //make the scenarios sortable
    $( "#tests" ).sortable({ handle: ".what" });
    //make the tables (example data) sortable
    $( "tbody" ).sortable();
    //setup our new dropdowns using the chosen plugin
    $('select').each(function(){
        var placeholder = $(this).attr('placeholder');
        if ( $(this).parent().is("td") ) {
            placeholder = " ";
        }
        if ( $(this).attr('multiple') == 'multiple' ) {
            $(this).chosen({
                placeholder_text_multiple: placeholder,
                width: 'auto',
                disable_search_threshold: 4,
                inherit_select_classes: true,
            });
        } else {
            $(this).chosen({
                placeholder_text_single: placeholder,
                width: $(this).width()+15,
                disable_search_threshold: 4,
                inherit_select_classes: true,
            });
        }
    });
    //allow editing of an any span
    $('span.any').dblclick(function(){
        var input = $( "<input type='text'/>" ).keyup(resizeInput).each(resizeInput).blur(function(){
            if( $(this).val() == "" ) {
                $(this).parent().html( "..." );
            }
        });
        $(this).html( input );
    });
    //allow the toggling an opt span
    $('span.opt').dblclick(function(){
        if ( $(this).css('opacity') > 0.5 )
            $(this).css({ opacity: 0.2 });
        else
            $(this).css({ opacity: 1 });
    });
    //keep the size of our input fields under control
    $('input').each(function(){
        $(this).keyup(resizeInput).each(resizeInput);
    });
    //keep the size of our textareas under control
    $('textarea').keyup(function(){
        $(this).attr('rows',($(this).val().split("\n").length||1));
    });
}
function addScenario() {
    $('#tests').append(
            "<div class='scenario'>" +
                "<input class='purple' placeholder='Scenario Tags' />" +
                "<div class='green'>" +
                    "<span class='what'>Scenario:</span> <input class='green' placeholder='Test Case Name' type='text' />" +
                    "<br/>" +
                    "<textarea rows='1' placeholder='Test Case Description'></textarea>" +
                "</div>" +
                "<div class='testSteps'></div>\n" +
                "<button onclick='addTestStep(this)'>Add Test Step</button>" +
                "<button onclick='addDataTable(this)' class='addTable' style='display:none;'>Add Data Table</button>" +
                "<div class='delete' onclick='del(this)' style='top:60px;'>&nbsp;</div>" +
            "</div>"
    );
    makeDynamic();
}
function addTestStep(el) {
    $(el).parent().find('.testSteps').append(
            "<div class='testStep'><div class='edit' onclick='edit(this)'>&nbsp;</div><div class='delete' onclick='del(this)'>&nbsp;</div><select class='blue' onchange='fillStep(this)'><option></option><option>Given</option><option>When</option><option>Then</option></div>"
    );
    makeDynamic();
}
function fillStep(el) {
    var value = $(el).val();
    $(el).next().nextAll().remove();
    var input = $("<input type='text' />");
    var autocompletes = new Array();
    if ( value == "Given" || value == "When" ) {
        for (i=0;i<testSteps.whens.length;i++) {
            autocompletes.push({
                label: testSteps.whens[i].string.stripTags(),
                value: testSteps.whens[i].string,
                what: "whens",
                order: i
            });
        }
    } else if ( value == "Then" ) {
        for (i=0;i<testSteps.thens.length;i++) {
            autocompletes.push({
                label: testSteps.thens[i].string.stripTags(),
                value: testSteps.thens[i].string,
                what: "thens",
                order: i
            });
        }
    }
    input.autocomplete({
        minLength: 0,
        source: autocompletes,
        focus: function( event, ui ) {
            input.val( ui.item.value.stripTags() );
            return false;
        },
        select: function( event, ui ) {
            fillVars(ui.item.what,ui.item.order,$(this));
        },
        minLength: 0,
    }).click(function(){
        $(this).autocomplete( "search", "" );
    }).blur(function(){
        createStep($(this));
    });
    $(el).next().after( input );
}
function createStep(el) {
    var newStep = $(el).val();
    var type = $(el).prev().prev();
    var step = "";
    //Need to determine if this step matches something
    //Check each GWT, replace XXXX with (.*), and do a regex check
    //if any are a match, select it
    if ( type.val() == "Given" || type.val() == "When" ) {
        step = "whens";
    } else if ( type.val() == "Then" ) {
        step = "thens"
    } else {
        return false;
    }
    for (i=0;i<testSteps[step].length;i++) {
        var string = testSteps[step][i].string;
        var regex = string.replace(/<span class='opt'>(.*?)<\/span>/g, "($1)?");
        var regex = regex.replace(/<span class='any'>(.*?)<\/span>/g, "(.*?)");
        regex = regex.replace(/XXXX/g, "(.*)");
        regex = regex.stripTags();
        regex = "^"+regex+"$";
        var res = newStep.match( regex );
        if ( res != null ) {
            fillVars(step,i,el);
            var inputs = type.parent().children('input,select');
            for(j=1;j<res.length;j++) {
                if( res[j] == "XXXX" ) {
                    res[j] = "";
                }
                $(inputs).eq(j).val( res[j] );
                if ( $(inputs).eq(j).is('select') ) {
                    $(inputs).eq(j).trigger('chosen:updated');
                }
                buildTable(type.parent().parent().parent());
                makeDynamic();
            }
            return true;
        }
    }
    type.next().nextAll().remove();
    var newStepPieces = newStep.split(/<|>/);
    newStep = "";
    for (i=0;i<newStepPieces.length;i++) {
        newStep += "<span class='new'> " + newStepPieces[i] + "</span>";
        if ( newStepPieces[i+1] ) {
            newStep += "<input id='"+rand(10)+"' type='text' onchange='buildTable(this)' placeholder='<"+newStepPieces[i+1]+">' class='new' />";
            i++;
        }
    }
    type.parent().append( newStep );
    buildTable(type.parent().parent().parent());
    makeDynamic();
}
function fillVars(what,order,el) {
    var sClass = $(el).parent().parent().parent().attr("class");
    var testStepString = testSteps[what][order].string;
    var testStepInputs = testSteps[what][order].inputs;
    var testStepPieces = testStepString.split("XXXX") //.filter(function(el) {return el.length != 0});
    var type = $(el).prev();
    type.nextAll().remove();
    for ( i=0;i<testStepPieces.length;i++) {
        type.parent().append( "<span> " + testStepPieces[i] + "</span>" );
        if ( testStepInputs[i] !== undefined ) {
            var objID = rand(10);
            if ( Object.prototype.toString.call( testStepInputs[i].value ) === '[object Array]' ) {
                var sel = $("<select id='"+objID+"' type='"+testStepInputs[i].key+"' onchange='buildTable(this)' placeholder='<"+testStepInputs[i].key+">'></select>");
                if ( testStepInputs[i].key.endsWith( "List" ) ) {
                    sel.attr("multiple","multiple");
                    sel.append( "<option></option>" );
                } else {
                    sel.append( "<option>&lt;"+testStepInputs[i].key+"&gt;</option>" );
                }
                for (j=0;j<testStepInputs[i].value.length;j++) {
                    sel.append( "<option>" + testStepInputs[i].value[j] + "</option>" );
                }
                type.parent().append( sel );
            } else {
                type.parent().append( "<input id='"+objID+"' type='"+testStepInputs[i].value+"' onchange='buildTable(this)' placeholder='<"+testStepInputs[i].key+">' />" );
            }
        }
    }
    buildTable(type.parent().parent().parent());
    makeDynamic();
}
function addDataTable(el) {
    addTable( $(el).parent() );
    buildTable( $(el).parent() );
}
function buildTable(testEl) {        //el should be the test element
    if ( !$(testEl).hasClass( 'scenario' ) ) {
        testEl = $(testEl).parent().parent().parent();
    }
    if ( !$(testEl).hasClass( 'scenario' ) ) {
        //TODO - remove this, just for error checking
        //alert( "FAIL!" );
        return false;
    }
    //add a table if needed
    if ( $(testEl).find( '.examples' ).length == 0 ) {
        addTable( $(testEl) );
        $(testEl).children('.addTable').show();
    }
    //get each variable from our test steps
    var variables = new Object();
    var scenario = $(testEl).children('.testSteps');
    scenario.children('.testStep').each(function(){
        $(this).children('input, select').each(function(){
            if ( $(this).val() == null ) {
                var placeholder = $(this).attr('placeholder')
                if ( placeholder.startsWith("<") && placeholder.endsWith(">") ) {
                    variables[ placeholder.substring(1,placeholder.length-1) ] = $(this).attr('id');
                }
                return true;
            }
            if ( Object.prototype.toString.call( $(this).val() ) === '[object Array]' ) {
                return true;
            }
            if ( $(this).hasClass('ui-autocomplete-input') ) {
                return true;
            }
            if ( $(this).val() == "" && $(this).is("input") ) {
                var placeholder = $(this).attr('placeholder')
                variables[ placeholder.substring(1,placeholder.length-1) ] = $(this).attr('id');
                return true;
            }
            if ( $(this).val().startsWith("<") && $(this).val().endsWith(">") ) {
                var placeholder = $(this).val();
                variables[ placeholder.substring(1,placeholder.length-1) ] = $(this).attr('id');
                return true;
            }
        });
    });
    var examples = scenario.parent().children('.examples');
    $(examples).each(function(){
        example = $(this);
        //remove any columns that no longer exist in the variables
        var header = example.children('table').children('thead').children('tr');
        var headerVals = new Array();
        header.children('th').each(function(){
            headerVals.push( $(this).html() );
            if ( !variables.hasOwnProperty($(this).html()) ) {
                var column = $(this).parent().children().index(this);
                example.find('tr').each(function(){
                    $(this).find("th:eq("+column+")").remove();
                    $(this).find("td:eq("+column+")").remove();
                })
            }
        });
        //add in any new values that may be missing
        for ( var key in variables ) {
            if( $.inArray( key, headerVals ) == -1 ) {
                header.append( "<th inheritFrom='"+variables[key]+"'>" + key + "</th>" );
                example.children('table').children('tbody').children('tr').each(function(){
                    var cell = $("<td></td>");
                    var input = $("#"+variables[key]).clone();
                    input.removeAttr('id');
                    input.removeAttr('style');
                    input.removeAttr('onchange');
                    input.val('');
                    if ( input.is("input") ) {
                        input.removeAttr('placeholder');
                    }
                    if ( input.is("select") ) {
                        input.find(">:first-child").remove();
                    }
                    cell.append( input );
                    $(this).append( cell );
                });
            }
        };
    });
    //rearrange our table
    var order = new Array();
    for (var key in variables) {
        order.push(key);
    }
    console.log( order );
    var newOrder = new Array();
    examples.find('table').first().find('th').each(function(){
        newOrder.push( jQuery.inArray($(this).html(), order) );
    });
    console.log( newOrder );

    makeDynamic();
    //rename our scenario type if needed
    var cols = examples.children('table').children('thead').children('tr').children('th');
    if ( cols.length == 0 ) {
        examples.parent().children('.green').children('.what').html( "Scenario:" );
        examples.remove();
        $(testEl).children('.addTable').hide();
    }
}
function addTable(el) {
    $(el).append(
        "<div class='examples'>" +
            "<input class='purple' placeholder='Example Tags' />" +
            "<div class='green'>Examples:</div>" +
            "<table><thead><tr></tr></thead><tbody></tbody></table>" +
            "<button onclick='addDataRow(this)'>Add Data Row</button>" +
        "</div>"
    );
    $(el).children('.green').children('.what').html( "Scenario Outline:" );
    addDataRow( $(el).children('.examples').last().children('table') );
}
function addDataRow(el) {
    var examples = $(el).parent();
    var body = examples.children('table').children('tbody');
    var cols = examples.children('table').children('thead').children('tr').children('th');
    var row = $("<tr></tr>");
    cols.each(function(){
        var cell = $("<td></td>");
        var input = $("#"+$(this).attr('inheritFrom')).clone();
        input.removeAttr('id');
        input.removeAttr('style');
        input.removeAttr('onchange');
        if ( input.is("input") ) {
            input.removeAttr('placeholder');
        }
        if ( input.is("select") ) {
            input.find(">:first-child").remove();
        }
        cell.append( input );
        row.append( cell );
    });
    body.append( row );
    makeDynamic();
}
function del(el) {
    var r=confirm("Are you sure you want to delete this " + $(el).parent().attr('class') );
    if (r==true) {
        tmp = $(el).parent().parent().parent()
        $(el).parent().remove();
        buildTable( $(tmp) );
    }
}
function edit(el) {
    var r=confirm("Are you sure you want to edit this " + $(el).parent().attr('class') );
    if (r==true) {
        tmp = $(el).parent().parent().parent()
        fillStep( $(el).next().next() );
        buildTable( $(tmp) );
    }
}