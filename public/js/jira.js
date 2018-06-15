$(function() {
    fillLink($('#featLink'));
});

function existingFeature() {
    if ($("#jiraFeat").is(":visible")) {
        $("#featLink").show();
        $("#featTag").show();
        $("#featuredef").show();
        $("#jiraFeat").hide();
        $("#exportFile").button("enable");
        $('.link').show();
        $('.tag').show();
    } else {
        $("#featLink").hide();
        $("#featTag").hide();
        $("#featuredef").hide();
        $("#jiraFeat").show();
        $("#exportFile").button("disable");
        $('.link').hide();
        $('.tag').hide();
    }
    checkRequired($("#jiraFeat"));
}

function fillLink(el) {
    $(el).keyup(function(e) {
        if(e.keyCode == 32 || e.keyCode == 13){
            addLink(el);
        }
    }).blur(function(){
        addLink(el);
    });
}
function addLink(el, link) {
    if (link == "" || link === undefined ) {
        link = $(el).val();
        if (link == "") {
            return;
        }
    }
    // build the environment
    var span = $("<span>");
    span.html(link);
    span.addClass('link');
    span.click(function(){
        $(this).remove();
    });
    $(el).after(span);
    $(el).val("");
}