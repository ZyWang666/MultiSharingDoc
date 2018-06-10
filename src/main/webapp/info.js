function tii(p, q) {
    if (parseInt(p.pos) < parseInt(q.pos) || (parseInt(p.pos) == parseInt(q.pos) && p.uid > q.uid)) {
        return [
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos),
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos)+1,
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    } else {
        return [
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos)+1,
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos),
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    }
}

function tid(p, q) {
    if (parseInt(p.pos) <= parseInt(q.pos)) {
        return [
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos),
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos)+1,
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    } else {
        return[
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos)-1,
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos),
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    }
}

function tdi(p, q) {
    if (parseInt(p.pos) < parseInt(q.pos)) {
        return [
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos),
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos)-1,
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    } else {
        return [
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos)+1,
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos),
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    }
}

function tdd(p, q) {
    if (parseInt(p.pos) < parseInt(q.pos)) {
        return [
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos),
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos)-1,
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    } else if (parseInt(p.pos) > parseInt(q.pos)) {
        return [
            {
                "opcode": p.opcode,
                "documentId": p.documentId,
                "pos": parseInt(p.pos)-1,
                "payload": p.payload,
                "uid": p.uid,
            },
            {
                "opcode": q.opcode,
                "documentId": q.documentId,
                "pos": parseInt(q.pos),
                "payload": q.payload,
                "uid": q.uid,
            }
        ]
    } else {
        return [
            {
                "opcode": "IDENTITY",
            },
            {
                "opcode": "IDENTITY",
            }
        ]
    }
}

function transform(p, q) {
    if (p == null || q == null) {
        return [p,q];
    }
    if (p.opcode == "INSERT" && q.opcode == "INSERT") {
        return tii(p, q);
    } else if (p.opcode == "INSERT" && q.opcode == "DELETE") {
        return tid(p, q);
    } else if (p.opcode == "DELETE" && q.opcode == "INSERT") {
        return tdi(p, q);
    } else if (p.opcode == "DELETE" && q.opcode == "DELETE") {
        return tdd(p, q);
    }
    return [p,q];
};

function transformMultiple(ps, q) {
    var ret = [];
    if (q == null || ps.length == 0) {
        return [ret, q];
    }

    for (i = 0; i < ps.length; i+=1) {
        transformL = transform(ps[i], q);
        ret.push(transformL[0]);
        q = transformL[1];
    }
    return [ret, q];
};

(function() {
    _showFileContent = function(data) {
        ret = JSON.parse(data);
        $("#text").show();
        text = ret.document;
        ver = ret.ver;
        sessionStorage.setItem("version", ver);
        $("#text textarea").val(text);
    };

    showFileContent = function() {
        fileName = $(this).text();
        sessionStorage.setItem("fileName", fileName);
        $.ajax({
            url: "/documents/d?documentId="+fileName,
            type: "GET",
            success: _showFileContent,
        });
        $("#theFileName").html(fileName);
        return false;
    };

    updateFile = function(name) {
        ul = $("#updateFiles");
        ul.show();
        ul.append('<li><a href="#">' + name + '</a></li>');
        $("#allFiles li").click(showFileContent);
    };

    addFile = function() {
        var name;
        name = $("form#addfile input#filename").val();
        if(name === "") {
            return false;
        }
        $("form#addfile input#filename").val("");
        $.ajax({
            url: "/documents?name="+name,
            type: "POST",
            data: name,
            success: updateFile(name),
        });
        return false;
    };

    _showFiles = function(data) {
        $("#adduser").hide();
        $("#allUsers").hide();

        $("#userList h2").html("Files");
        files = $("#allFiles");
        files.show();

        $("#addfile").show();
        ul = $("#updateFiles");
        var name, ret, ul, users, _i, _len, _ref;
        ret = JSON.parse(data);
        if(ret == null || ret.length == 0) {
            return;
        }

        _ref = ret;
        for(_i=0, _len=_ref.length; _i < _len; _i++) {
            name = _ref[_i];
            ul.append('<li><a href="#">' + name + '</a></li>');
        }
        $("#allFiles li").click(showFileContent);
    };

    showUserFiles = function() {
        uid = $(this).text();
        sessionStorage.setItem("uid", uid);
        $.ajax({
            url: "/documents",
            type: "GET",
            success: _showFiles,
            cache: false
        });
    };

    updateUser = function(name) {
        ul = $("#updateUsers");
        ul.append('<li><a href="#">' + name + '</a></li>');
        $("#allUsers li").click(showUserFiles);
    };

    updateUsers = function(data) {
        var name, ret, ul, users, _i, _len, _ref;
        ret = JSON.parse(data);
        users = $("#allUsers");

        if(ret == null) {
            users.append("No user.");
            return;
        }

        ul = $("#updateUsers");
        _ref = ret;

        for(_i=0, _len=_ref.length; _i < _len; _i++) {
            name = _ref[_i];
            ul.append('<li><a href="#">' + name + '</a></li>');
        }
        $("#allUsers li").click(showUserFiles);
    };

    listUsers = function() {
        $.ajax({
            url: "/users",
            type: "GET",
            success: updateUsers,
            cache: false
        });
    };

    addUser = function() {
        var name;
        name = $("form#adduser input#username").val();
        if(name === "") {
            return false;
        }
        $("form#adduser input#username").val("");
        $.ajax({
            url: "/users?user="+name,
            type: "POST",
            data: name,
            success:updateUser(name)
        });
        return false;
    };

    _autoUpdate = function(data) {
        receiveAck = false
        ret = JSON.parse(data);
        if(ret == null) {
            return;
        }

        for(i = 0; i < ret.length; i++) {
            op = ret[i].opcode;
            pos = ret[i].pos;
            payload = ret[i].payload;
            uid = ret[i].uid;
            ver = ret[i].version;

            version = sessionStorage.getItem("version");
            sessionStorage.setItem("version", (parseInt(version)+1).toString());
            end = $("#text textarea").selectionEnd;

            // receive server ACK, must have one or more pending request
            // (but only one outstanding request)
            if(uid == sessionStorage.getItem("uid")) {
                receiveAck = true
            } else {
                // transform incoming operation against current outstanding and buffer operations
                outstandingOp = JSON.parse(sessionStorage.getItem("outstandingOp"));
                bufferedOps = JSON.parse(sessionStorage.getItem("bufferedOps"));

                transformL = transform(outstandingOp, ret[i]);
                outstandingOp_t = transformL[0];
                operation_t = transformL[1];

                transformLL = transformMultiple(bufferedOps, operation_t);
                bufferedOps = transformLL[0];
                operation_tt = transformLL[1];

                sessionStorage.setItem("outstandingOp", JSON.stringify(outstandingOp_t));
                sessionStorage.setItem("bufferedOps", JSON.stringify(bufferedOps));

                if (operation_tt.opcode == "DELETE") {
                    newText = $("#text textarea").val().substring(0,operation_tt.pos+1) +
                            $("#text textarea").val().substring(operation_tt.pos+2,
                            $("#text textarea").val().length);
                    $("#text textarea").val(newText);
                } else if (operation_tt.opcode == "INSERT") {
                    newText = $("#text textarea").val().substring(0,operation_tt.pos) + operation_tt.payload +
                            $("#text textarea").val().substring(operation_tt.pos,
                            $("#text textarea").val().length);

                    $("#text textarea").val(newText);
                } else if (operation_tt.opcode == "IDENTITY") {
                    continue;
                }
            }
            $("#text textarea").selectionEnd = end;
        }

        if (receiveAck) {
            bufferedOps = JSON.parse(sessionStorage.getItem("bufferedOps"));
            // if there is only one pending request, then transition back to synchronized state.
            if(bufferedOps.length == 0) {
                sessionStorage.setItem("ACK","T");
                sessionStorage.setItem("outstandingOp", JSON.stringify(null));
                sessionStorage.setItem("bufferedOps", JSON.stringify([]));
            // if there are more than one pending requests, then stay in current state.
            } else {
                // this is the operation variable
                operation = bufferedOps[0];
                operation.version = parseInt(sessionStorage.getItem("version")).toString();
                // store the current outstanding operation
                sessionStorage.setItem("outstandingOp", JSON.stringify(operation));

                //send transformed operation to server
                $.ajax({
                    type: "POST",
                    url: "/documents/op",
                    contentType: "application/json", // NOT dataType!
                    data: JSON.stringify(operation),
                });

                //remove current operation from bufferOps
                bufferedOps.splice(0, 1);
                sessionStorage.setItem("bufferedOps", JSON.stringify(bufferedOps));
            }
        }
        // autoUpdate();
    }

    autoUpdate = function() {
        fileName = $("#theFileName").html();
        ver = sessionStorage.getItem("version");
        console.log("autoUpdate");
        if(fileName != "") {
            $.ajax({
                url: "/documents/op?documentId=" + fileName + "&version=" + ver,
                type: "GET",
                success: function(data, status, jqXHR) {
                    console.log("succ")
                    _autoUpdate(data);
                    setTimeout(autoUpdate, 10);
                },
                error: function(jqXHR, status, errorThrown) {
                    console.log("err")
                    if (status == 'timeout') {
                        setTimeout(autoUpdate, 10);
                    } else {
                        setTimeout(autoUpdate, 5000);
                    }
                },
                timeout: 5000
            });
        } else {
            setTimeout(autoUpdate, 5000);
        }
    }
    //TODO TEST MODIFIED: parameter
    webPost = function(test, pos, payload) {
        // pos = $('#text textarea').prop("selectionStart");
        documentId = sessionStorage.getItem("fileName");
        uid = sessionStorage.getItem("uid");
        version = sessionStorage.getItem("version");
        if(test != true) {
          pos = $('#text textarea').prop("selectionStart");
          payload = window.event.key;
        }
        else {
          console.log("test");
        }

        op = "INSERT";
        if (payload == "Backspace") {
            payload = "";
            op = "DELETE";
        } else if (payload.length != 1) {
            return;
        }

        var data = {
            documentId: documentId,
            pos: pos-1,
            payload: payload,
            opcode: op,
            uid: uid,
            version: version,
        };

        if (sessionStorage.getItem("ACK") === "T") {
            sessionStorage.setItem("ACK", "F");
            sessionStorage.setItem("outstandingOp", JSON.stringify(data))
            $.ajax({
                type: "POST",
                url: "/documents/op",
                contentType: "application/json", // NOT dataType!
                data: JSON.stringify(data),
            });
        } else {
            bufferedOps = JSON.parse(sessionStorage.getItem("bufferedOps"));
            bufferedOps.push(data);
            sessionStorage.setItem("bufferedOps", JSON.stringify(bufferedOps));
        }
    };

    main = function() {
        // setInterval(autoUpdate, 1000);
        
        sessionStorage.setItem("ACK", "T");
        sessionStorage.setItem("outstandingOp", JSON.stringify(null));
        sessionStorage.setItem("bufferedOps", JSON.stringify([]));

        autoUpdate();

        $("form#adduser").submit(addUser);
        $("form#addfile").submit(addFile);
        $("#text textarea").keyup(webPost);
    };

    $(document).ready(main);
    $(document).ready(listUsers);

}).call(this);
