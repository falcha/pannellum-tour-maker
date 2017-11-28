
$("#form-upload").validate({
    errorElement: 'div',
    rules: {
        name: {
            required: true
        },
        file: {
            required: true
        }
    },

    messages: {
        name: {
            required: "Tour name is required."
        },
        file: {
            required: "Photo zip file is required."
        }
    },

    submitHandler: function (form) {
        var name = $("#input-tourname").val();
        var type = $("[name='type']:checked").val();
        var file = $("#input-file")[0].files[0];

        var form = new FormData();
        form.append("file", file);
        form.append("name", name);
        form.append("type", type);

        var options = {
            method: "POST",
            mimeType: "multipart/form-data",
            data: form,
            cache: false,
            contentType: false,
            processData: false
        };

        $.ajax(apiUrl + "/public/guest/tours", options)
            .done(function (response, status, xhr) {
                if (xhr.status === 200) {
                    Materialize.toast('Photos were successfully uploaded to the server. ' +
                        'It might take a moment for the server to process these photos. ' +
                        'You will find the successfully processed tour in the tour list.', 10000)
                }
            })
            .fail(function(xhr) {
                var message = JSON.parse(xhr.responseText).message;
                Materialize.toast('Photos upload failed. ' + message, 10000)
            });
    }
});