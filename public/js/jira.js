$(function() {

});

function existingFeature() {
    if ($("#jiraFeat").is(":visible")) {
        $("#featLink").show();
        $("#featTag").show();
        $("#featuredef").show();
        $("#jiraFeat").hide();
    } else {
        $("#featLink").hide();
        $("#featTag").hide();
        $("#featuredef").hide();
        $("#jiraFeat").show();
    }
    checkRequired($("#jiraFeat"));
}

function link() {
    alert("TBD");
}