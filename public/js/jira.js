$(function() {

});

function existingFeature() {
    if ($("#jiraFeat").is(":visible")) {
        $("#featLink").show();
        $("#featTag").show();
        $("#featuredef").show();
        $("#jiraFeat").hide();
        $("#exportFile").button("enable");
    } else {
        $("#featLink").hide();
        $("#featTag").hide();
        $("#featuredef").hide();
        $("#jiraFeat").show();
        $("#exportFile").button("disable");
    }
    checkRequired($("#jiraFeat"));
}

function link(el) {
    $(el).hide();
    var linkInput = $("<input class='green small jiralink' placeholder='Existing JIRA Development Issue Keys, space separated'>");
    $(el).parent().find('.purple').after(linkInput);
}