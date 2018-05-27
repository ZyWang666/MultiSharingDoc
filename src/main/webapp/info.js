(function() {
  _showFileContent = function(data) {
    ret = JSON.parse(data);
    $("#text").show();
    text = ret.document;
    ver = ret.ver;
    //$("#storedver").html(ver);
    localStorage.setItem("version", ver);
    $("#text textarea").val(text);
  };

  showFileContent = function() {
    fileName = $(this).text();
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
    //$("#storeduid").html(uid);
    localStorage.setItem("uid", uid);
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
    console.log("_autoUpdate");
    ret = JSON.parse(data);
    if(ret == null) {
      return;
    }

    for(i = 0; i < ret.length; i++) {
      console.log("length: " + i);
      op = ret[i].opcode;
      pos = ret[i].pos;
      payload = ret[i].payload;
      uid = ret[i].uid;
      ver = ret[i].version;
/*
      console.log("op: " + op);
      console.log("pos: " + pos);
      console.log("payload: " + payload);
      console.log("uid: " + uid);
      console.log("ver: " + ver);
*/
      //$("#storedver").html(ver);
      localStorage.setItem("version", ver);
      end = $("#text textarea").selectionEnd;

      if(uid == localStorage.getItem("uid"))
      {
        obj = JSON.parse(localStorage.getItem("bufferedOps"));
        if(obj.operations.length == 0)
          localStorage.setItem("ACK","T");
        else
        {
          //this is the operation variable
          operation = obj.operations[0];

          //this is the local copy of data
          txt = $("#text textarea").val();

          //this is the current verion of the local copy
          version = localStorage.getItem("version");

          //TODO: transform operation

          //send transformed operation to server
          $.ajax({
            type: "POST",
            url: "/documents/op",
            contentType: "application/json", // NOT dataType!
            data: operation,
          });


          //remove current operation from bufferOps
          obj.operations.splice(0, 1);
          localStorage.setItem("bufferedOps", JSON.stringify(obj));
        }
      }

      else if(op == "DELETE")
      {

        newText = $("#text textarea").val().substring(0,pos+1)+
                    $("#text textarea").val().substring(pos+2,
                    $("#text textarea").val().length);

        $("#text textarea").val(newText);


      }
      else if(op == "INSERT")
      {
        newText = $("#text textarea").val().substring(0,pos)+ payload +
                    $("#text textarea").val().substring(pos,
                    $("#text textarea").val().length);

        $("#text textarea").val(newText);
      }
      else if(op == "IDENTITY")
      {
        continue;
      }
      $("#text textarea").selectionEnd = end;
    }
  }

  autoUpdate = function() {
    fileName = $("#theFileName").html();
    //ver = $("#storedver").html();
    ver = localStorage.getItem("version");
    if(fileName != "")
    {
      $.ajax({
        url: "/documents/op?documentId="+fileName + "&ver=" + ver,
        type: "GET",
        success:_autoUpdate
      });
    }
  }
  main = function() {
    setInterval(autoUpdate, 3000);

    localStorage.setItem("ACK", "T");
    localStorage.setItem("bufferedOps", '{"operations":[]}');

    $("form#adduser").submit(addUser);
    $("form#addfile").submit(addFile);
    $("#text textarea").keyup(function(){
      pos= $('#text textarea').prop("selectionStart");
      payload = window.event.key;
      documentId = $("#theFileName").html();
      op = "ins";
      if(payload == "Backspace")
      {
        payload = "";
        op = "del";
      }
      else if(payload.length != 1)
        return;

      var data = {
        documentId: documentId,
        pos: pos-1,
        payload: payload,
        op: op,
        uid: localStorage.getItem("uid"),
        ver: localStorage.getItem("version"),
      };

      if(localStorage.getItem("ACK") === "T")
      {
        localStorage.setItem("ACK", "F");
        $.ajax({
          type: "POST",
          url: "/documents/op",
          contentType: "application/json", // NOT dataType!
          data: JSON.stringify(data),
        });
      }
      else {
        {
          obj = JSON.parse(localStorage.getItem("bufferedOps"));
          obj['operations'].push(JSON.stringify(data));
          localStorage.setItem("bufferedOps", JSON.stringify(obj));
        }
      }
    });
  };

  $(document).ready(main);
  $(document).ready(listUsers);

}).call(this);
