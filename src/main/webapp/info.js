  (function() {
    _showFileContent = function(data) {
      ret = JSON.parse(data);
      $("#text").show();
      text = ret.documentRope.sequence;
      $("#text textarea").val(text);
    };

    showFileContent = function() {

      fileName = $(this).text();
      $("#theFileName").html(fileName);
      $.ajax({
        url: "/documents/d?documentId="+fileName,
        type: "GET",
        success: _showFileContent,
      });
    };

    updateFile = function(name) {
      console.log("updateFile: " + name);
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

    main = function() {
      $("form#adduser").submit(addUser);
      $("form#addfile").submit(addFile);
      $("#text textarea").keyup(function(){
        pos= $('#text textarea').prop("selectionStart");
        payload = window.event.key;
        documentId = $("#theFileName").html();
        console.log(documentId + " " + payload + " " + pos);
        op = "ins";
        if(payload == "Backspace")
        {
          payload = "";
          op = "del";
        }
        else if(payload.length != 1)
          return;

        //except keys like 'shift', 'escape'...

        var data = {
          documentId: documentId,
          pos: pos-1,
          payload: payload,
          op: op,
        };

        $.ajax({
          type: "POST",
          url: "/documents/d",
          contentType: "application/json", // NOT dataType!
          data: JSON.stringify(data),
        });
      });
    };

    $(document).ready(main);
    $(document).ready(listUsers);

  }).call(this);
