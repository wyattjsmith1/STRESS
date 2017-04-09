var comparators = ["=", "<", ">", "<=", ">=", "!="];
var numberColumns = 4

var snapshotId = null;
var fields = {};
var filters;
var back = loadStartOptions;
var projects = null;
var usedFields = null;
var selectedProjects = null;
var selectionType = null;
var seed = "";
var projectCount = 5;
var captcha = '<div id="captcha"></div>';


window.onload = function() {
    loadStartOptions();
}

function clear() {
    $("#content").empty();
}

function loadStartOptions() {
    back = loadStartOptions;
    clear();
    $("#content").append($("<button/>", {
        class: "btn btn-primary btn-lg",
        text: "New search",
        click: function() {
            newSearch();
        }
    }));
    $("#content").append($("<br/>"));
    $("#content").append($("<button/>", {
        class: "btn btn-primary btn-lg",
        text: "Import search",
        click: function() {
            loadImport();
        }
    }));
}

function loadImport() {
    clear();
    back = loadStartOptions

    var form = $('<form></form>');
    var files = $('<input type="file" id="files" name="file"/>');
    function handleFileSelect(e) {
        e.preventDefault();
        var files = e.target.files;
        var reader = new FileReader();
        reader.onload = (function(theFile) {
            return function(e) {
                var data = JSON.parse(e.target.result);
                snapshotId = data.type.snapshotId;
                filters = data.filters;
                selectionType = data.type;
                usedFields = data.usedFields;
                if (data.type.name == "random") {
                    seed = data.type.seed;
                    projectCount = data.type.number;
                }
                projects = data.projects;
                selectedProjects = [];
                var selectedSet = new Set(data.selectedIds);
                projects.forEach(function(item) {
                    if (selectedSet.has(item.id)) {
                        selectedProjects.push(item);
                    }
                });
                $.ajax({
                    url: "/fields",
                    dataType: "json",
                    error: function(j, status, error) {
                        alert("Unable to load fields: " + status + ": " + error);
                    },
                    success: function(data, status, j) {
                        fields = {};
                        for (var group in data) {
                            if (data.hasOwnProperty(group)) {
                                var groupFields = data[group];
                                groupFields.forEach(function(item) {
                                    fields[item.fieldName] = item;
                                });
                            }
                        }
                        loadSelection();
                    }
                });
            };
        })(files);

        reader.readAsText(files.files[0]);
    }
    form.append(files);
    form.append($('<input class="btn btn-primary" type="submit">Submit</input>'));
    $("#content").append(form);
    form.submit(handleFileSelect);

}

function newSearch() {
    back = loadStartOptions;
    clear();
    $("#content").append("<h2>Select a snapshot.</h2>");
    $.ajax({
        url: "/snapshots",
        dataType: "json",
        error: function(j, status, error) {
            alert("Unable to load snapshots: " + status + ": " + error);
        },
        success: function(data, status, j) {
            data.forEach(function(item) {
                var date = moment(item.started * 1000);
                $("#content").append($("<button/>", {
                    class: "btn btn-primary btn-lg",
                    text: date.format() + " (" + date.fromNow() + ")",
                    click: function() {
                        snapshotId = item.id;
                        loadFields();
                    }
                }));
                $("#content").append($('<br/>'));
            });
        }
    });
}

function loadFields() {
    back = newSearch;
    clear();
    $("#content").append("<h2>Select fields to filter by.</h2>");
    var div = $('<div align="left"></div>');
    var form = $("<form class='container' align='left'></form>");
    div.append(form);
    $("#content").append($("<button/>", {
        class: "btn",
        text: "All",
        click: function() {
            $(":checkbox").prop('checked', true);
        }
    }));
    $("#content").append($("<button/>", {
        class: "btn",
        text: "None",
        click: function() {
            $(":checkbox").prop('checked', false);
        }
    }));
    $("#content").append($("<button/>", {
        class: "btn",
        text: "Invert",
        click: function() {
            $.each(form.children(), function() {
                $.each($(this).children(), function() {
                    var box = $(this.firstChild);
                    box.prop('checked', !box.is(':checked'));
                });
            });
        }
    }));
    $("#content").append(div);
    $.ajax({
        url: "/fields",
        dataType: "json",
        error: function(j, status, error) {
            alert("Unable to load fields: " + status + ": " + error);
        },
        success: function(data, status, j) {
            var row = $('<div class="row"/>');
            fields = {};
            for (var group in data) {
                if (data.hasOwnProperty(group)) {
                    form.append($('<div class="row" align="center"><h4>' + group + '</h4></div>'));
                    var groupFields = data[group];
                    var row = $('<div class="row"></div>');
                    groupFields.forEach(function(item) {
                        var checked = laxContains(usedFields, item) ? "checked" : "";
                        row.append($('<div class="col-md-' + 12/numberColumns + '"><input type="checkbox" name="' + item.fieldName + '" ' + checked + '>' + item.displayName + '</input></div>'));
                        fields[item.fieldName] = item;
                    });
                    form.append(row);
                }
            }
        }
    });

    $("#content").append($("<button/>", {
        class: "btn btn-primary",
        text: "Next",
        click: function() {
            usedFields = [];
            form.serializeArray().forEach(function(item) {
                if (item.value == "on") {
                    usedFields.push(fields[item.name]);
                }
            });
            loadFilters();
        }
    }));
}

function laxContains(list, object) {
    if (!list) {
        return false;
    }
    for (var i = 0; i < list.length; i ++) {
        if (list[i].fieldName == object.fieldName) {
            return true;
        }
    }
    return false;
}

function loadFilters() {
    back = loadFields;
    clear();

    var form = $("<form></form>");
    //form.append(captcha);
    $("#content").append(form);
    if (filters) {
        filters.forEach(function(item) {
            createAndFilter(form, item);
        });
    } else {
        createAndFilter(form, null);
    }
    $("#content").append($("<button/>", {
        class: "btn",
        text: "AND",
        click: function() {
            createAndFilter(form, null);
        }
    }));

    var spinner = $('<br/><button class="btn btn-lg"><span class="glyphicon glyphicon-refresh spinning"></span> Loading...</button>');
    spinner.css('visibility', 'hidden');
    form.append($(captcha));
    grecaptcha.render("captcha", {
        sitekey: captchaPublic,
    });
    console.log("public", captchaPublic);
    form.append($("<button class='btn btn-primary'>Done</div>"));
    form.submit(function(e) {
        e.preventDefault();
        spinner.css('visibility', 'visible');
        var arr = $('form :input').serializeArray();
        filters = [];
        console.log(arr);
        for (i = 0; i < arr.length - 1; i += 3) {
            var next = {};
            for (j = 0; j < 3; j ++) {
                next[arr[i + j].name] = arr[i + j].value;
            }
            filters.push(next);
        }
        var send = {"filters": filters};
        var captchaResult = arr[arr.length - 1];
        send[captchaResult.name] = captchaResult.value;
        $.ajax({
            url: "/search/" + snapshotId,
            type: "POST",
            data: JSON.stringify(send),
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            processData: false,
            success: function(data, status, j) {
                grecaptcha.reset();
                spinner.css('visibility', 'hidden');
                if (data.length > 0 && $.isArray(data[0])) {
                    alert("Failed Captcha.");
                } else {
                    projects = data;
                    loadProjectView();
                }
            },
            error: function(j, status, error) {
                grecaptcha.reset();
                alert("Unable to search: " + status + ': ' + error);
                spinner.css('visibility', 'hidden');
            }
        });
    });
    $("#content").append(spinner);
}

function createAndFilter(root, obj) {
    var group = $("<div class='form-group'></div>");
    var field = $("<select name='field'></select>");
    var value = $("<input name='value' value=''></input>");
    if (obj != null) {
        value.val(obj.value);
    }
    usedFields.forEach(function(item) {
        var selected = obj != null && obj.field == item.fieldName ? "selected" : "";
        field.append($('<option value="' + item.fieldName + '" ' + selected + ' >' + item.displayName  + '</option>'));
    });
    field.on("change", function() {
        var f = fields[field.val()];
        value.attr('type', f.type);
        var val = f.type == "checkbox" ? "true" : '';
        value.attr('value', val);
    });
    value.change();
    var comparator = $("<select name='comparator'></select>");
    comparators.forEach(function(item) {
        var selected = obj != null && obj.comparator == item ? "selected" : "";
        comparator.append($('<option value="' + item + '" ' + selected + ' >' + item  + '</option>'));
    });
    root.append(group);
    group.append(field);
    group.append(comparator);
    group.append(value);
    group.append($("<button/>", {
        class: "btn btn-danger btn-xs",
        text: "-",
        click: function() {
            group.remove();
            return false; // Prevent form submit
        }
    }));
}

function getFieldFromString(string, obj) {
    console.log(string);
    string.split('.').splice(1).forEach(function (item) {
        console.log(obj, item);
        var match = new RegExp("^([a-z]+)\\['(.+)'\\]$").exec(item);
        console.log(match);
        if (match) {
            obj = obj[match[1]];
            obj = obj[match[2]];
        } else {
            obj = obj[item];
        }
    });
    return obj;
}

function loadProjectView() {
    clear();
    back = loadFilters;
    var form = $('<form></form>');
    form.append(captcha);
    var table = $('<table class="table table-bordered table-hover" style="width:100%"></table>');
    var header = $('<thead></thead>');
    var row = $('<tr></tr>');
    table.append(header);
    header.append(row);
    row.append($('<th>Select</th>'))
    usedFields.forEach(function(field) {
        row.append($('<th>' + field.displayName + '</th>'));
    });

    var body = $('<tbody></tbody>');

    console.log(projects);
    projects.forEach(function(project, index) {
        row = $('<tr></tr>');
        row.append($('<input type="checkbox" align="center" name=' + index + '></input>'))
        usedFields.forEach(function(field) {
            var value = getFieldFromString(field.fieldName, project);
            if (field.type == 'datetime-local' && value) {
                var m = moment(parseInt(value) * 1000);
                value = m.format('YYYY-MM-DD, HH:mm') + ' (' + m.fromNow() + ')';
            }
            if (field.fieldName == 'homepage') {
                row.append($('<td><a href="' + value + '">' + value + '</a></td>'));
            } else {
                row.append($('<td>' + value + '</td>'));
            }
        });
        body.append(row);
    });
    table.append(body);
    form.append(table);
    $("#content").append(form);
    $("#content").append($("<button/>", {
        class: "btn btn-primary",
        text: "Use Selection",
        click: function() {
            selectedProjects = [];
            form.serializeArray().forEach(function(item) {
                if (item.value == "on") {
                    selectedProjects.push(projects[parseInt(item.name)]);
                }
            });
            selectionType = {"snapshotId": snapshotId, "name": "manual"};
            loadSelection();
        }
    }));
    $("#content").append($("<button/>", {
        class: "btn btn-primary",
        text: "Random Seeded Selection",
        click: function() {
            $('#seed').show();
            $('#params').modal();
            $('#run').on("click", function(e) {
                selectedProjects = randomSelection($('#seed').val(), parseInt($('#projectCount').val()));
                $('#params').modal('hide')
                loadSelection();
            });
        }
    }));
    $("#content").append($("<button/>", {
        class: "btn btn-primary",
        text: "Systematic Sample",
        click: function() {
            $('#params').modal();
            $('#seed').hide();
            $('#run').on("click", function(e) {
                var s = $('#projectCount').val();
                systematicSample(parseInt(s));
                $('#params').modal('hide')
            });
        }
    }));
}

function systematicSample(number) {
    selectionType = {"name": "systematicSample", "snapshotId": snapshotId};
    $.ajax({
        url: "/sample/" + snapshotId,
        type: "POST",
        data: JSON.stringify({"projects": projects.map(function(item) {return item.id}),
                                "fields": usedFields.map(function(item) {return item.fieldName}),
                                "numberSamples": number}),
        contentType: "application/json; charset=utf-8",
        dataType:"json",
        processData: false,
        success: function(data, status, j) {
            selectedProjects = [];
            data.forEach(function (item) {
                projects.forEach(function (all) {
                    if (all.id == item) {
                        selectedProjects.push(all);
                    }
                });
            });
            loadSelection();
        },
        error: function(j, status, error) {
            alert("Unable to do systematic sample: " + status + ': ' + error);
        }
    });
}

function randomSelection(seed, number) {
    selectionType = {"name": "random", "seed": seed, "number": number, "snapshotId": snapshotId};
    if (number >= projects.length) {
        return projects;
    } else if (number <= 0) {
        return [];
    }
    var result = [];
    Math.seedrandom(seed);

    var indices = [];
    for (var i = 0; i < projects.length; i++) {
        indices.push(i);
    }

    while (number --) {
        var rand = Math.floor(Math.random() * indices.length);
        result.push(projects[indices[rand]]);
        indices.splice(indices.indexOf(rand), 1);
    }
    return result;
}

function loadSelection() {
    clear();
    back = loadProjectView;

    console.log(selectedProjects);

    var list = $('<ul class="list-group"></ul>');
    selectedProjects.forEach(function(project) {
        list.append('<li class="list-group-item">' + project.projectName + '</li>')
    });
    var selectedIds = selectedProjects.map(function(val) {return val.id});

    $("#content").append(list);
    $("#content").append($('<a/>', {
        class: "btn btn-primary",
        text: "Export Rationale",
        download: "data.json",
        href: "data:application/json," + encodeURIComponent(JSON.stringify({"type": selectionType, "filters": filters, "projects": projects, "selectedIds": selectedIds, "usedFields": usedFields}))
    }));
}