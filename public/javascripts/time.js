(function () {
    $.ajax("/current/America/Los_Angeles?token=xxx", {
        accepts: {json: "application/json" }
    })
        .done(function (json) {
            $('#time').text(json.result);
        })
        .fail(function (jqxhr, textStatus, error) {
            var err = textStatus + ', ' + error;
            $('#time').text(err);
        });
})();