function download() {
    //build our data
    data = "";
    if ( $('#featTag').val() != "" ) {
        data += $('#featTag').val() + "\n";
    }
    data += "Feature: " + $('#featuredef input').val() + "\n";
    if ( $('#featuredef textarea').val() != "" ) {
        data += $('#featuredef textarea').val() + "\n";
    }
    data += "\n";
    $('.scenario').each(function(){
        if ( $(this).children('input.purple').val() != "" ) {
            data += $(this).children('input.purple').val() + "\n";
        }
        data += $(this).children('div.green').children('span.what').html() + " ";
        data += $(this).children('div.green').children('input.green').val() + "\n";
        if ( $(this).children('div.green').children('textarea').val() != "" ) {
            data += $(this).children('div.green').children('textarea').val() + "\n";
        }
        $(this).find('.testStep').each(function(){
            $(this).children('input,select,span').each(function(){
                if ( ( $(this).val() == "" || $(this).val() == null )
                        && ( $(this).is("input") || $(this).is("select") ) ) {
                    if ( $(this).attr('placeholder') !== undefined ) {
                        data += $(this).attr('placeholder');
                    }
                } else if ( $(this).is('select') || $(this).is('input') ) {
                    if ( $(this).val() !== undefined ) {
                        data += $(this).val();
                    }
                } else {
                    if ( $(this).html() !== undefined ) {
                        data += $(this).html().stripTags();
                    }
                }
            });
            data += "\n";
        });
        if ( $(this).children('.examples').length ) {
            var examples = $(this).children('.examples');
            $(examples).each(function(){
                example = $(this);
                if ( example.children('input.purple').val() != "" ) {
                    data += example.children('input.purple').val() + "\n";
                }
                data += "Examples: \n";
                example.find('tr').each(function(){
                    $(this).find('th').each(function(){
                        data += "|\t"+$(this).html()+"\t";
                    });
                    $(this).children('td').children('input,select').each(function(){
                        data += "|\t"
                        if ( $(this).val() != null ) {
                            data += $(this).val();
                        }
                        data += "\t";
                    });
                    data += "|\n";
                });
            });
        }
        data += "\n";
    });
    //fix brackets
    data = data.replace(/</g, '&lt;');
    data = data.replace(/>/g, '&gt;');
    //fix extra spaces
    data = data.replace(/&nbsp;/g, ' ');
    data = data.replace(/( ){2,}/g, ' ');
    //download the file
    document.location = 'data:Application/octet-stream,' +
    encodeURIComponent( data );
    //warn the user about filename and linebreaks
    alert( "Note: this file will need to be renamed.\n\nOpen in wordpad or np++ to preserve line breaks.")
}