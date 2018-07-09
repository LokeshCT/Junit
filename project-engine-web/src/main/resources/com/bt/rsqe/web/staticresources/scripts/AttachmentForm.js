var rsqe = rsqe || {};

rsqe.AttachmentForm = function (opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.cancelButtonId = ".dialog .cancel";
    this.yesButtonId = "#yes";
    this.noButtonId = "#no";
    this.attachmentFormId = "#AttachmentDialogForm";

    this.progressDialogContainer = $("#progressDialog");
    this.errorMessages = $("#errorMessages", this.progressDialogContainer);

    this.progressDialog = new rsqe.ProgressDialog("#progressDialog", {
        progressText:"#progressText",
        progressButton:"#progressButton",
        yesButton:"#yes",
        noButton:"#no",
        errorClass:"error"
    });
    this.setupProgressDialog();
};

rsqe.AttachmentForm.prototype = {

    setUpAttachmentDialog:function () {
        var that = this;

        this.columnMetaData = [
            {"mDataProp":"uploadAppliesTo", sWidth:"7%"},
            {"mDataProp":"uploadFileName", sClass:"", sWidth:"10%"},
            {"mDataProp":"uploadDate", sClass:"", sWidth:"15%"},
            {"mDataProp":"id", sClass:"", sWidth:"5%"}
        ];

        this.userMessage = $('#message');
        this.tierFilter = $('#tierFilter');
        this.attachmentDataTableId = $("#attachmentTableItems");


        $("#tierFilter").prop('selectedIndex', 1);

        var selectedCategory = $("#tierFilter").val();
        var loadAttachmentUrl = $("#loadAttachmentActionUrl").val();

        var sAjaxSource = loadAttachmentUrl + '?categoryId=' + selectedCategory;

        var attachmentTable = $(that.attachmentDataTableId).dataTable({
                                                                          sPaginationType:"full_numbers",
                                                                          sDom:'lrt<"table_footer"ip>',
                                                                          sAjaxSource:sAjaxSource,
                                                                          bAutoWidth:false,
                                                                          bProcessing:true,
                                                                          bServerSide:true,
                                                                          bDeferRender:true,
                                                                          bSort:false,
                                                                          bRetrieve:true,
                                                                          bLengthChange:true,
                                                                          bScrollCollapse:false,
                                                                          iDisplayStart:0,
                                                                          sScrollY:($(window).height() - 650),
                                                                          bStateSave:true,
                                                                          bFilter:true,
                                                                          iDisplayLength:10,
                                                                          aLengthMenu:[1, 5, 10, 20, 100],
                                                                          aoColumns:that.columnMetaData,
                                                                          sAjaxDataProp:function (data) {
                                                                              return $.makeArray(data.itemDTOs);
                                                                          },
                                                                          fnRowCallback:function (row, aData) {
                                                                              return that.fnAttachmentRowCallback(row, aData)
                                                                          },
                                                                          fnDrawCallback:function () {
                                                                              that.bindDeleteClick();
                                                                              $(window).unbind('resize').bind('resize', function () {
                                                                                  var sScrollY = ($(window).height() - 500);
                                                                                  $(".dataTables_scrollBody").css("height", sScrollY);
                                                                                  $(self.attachmentDataTableId).dataTable().fnAdjustColumnSizing(false);
                                                                              });
                                                                          },
                                                                          oLanguage:{
                                                                              sInfo:"Showing _START_ to _END_ of _TOTAL_ attachments",
                                                                              sInfoEmpty:"Showing 0 to 0 of 0 attachments",
                                                                              sInfoFiltered:"- filtered from _MAX_ attachments",
                                                                              sZeroRecords:"No attachments found for the selected category.",
                                                                              sLengthMenu:"Show _MENU_ attachments"
                                                                          },
                                                                          "fnServerParams":function (aoData) {
                                                                             aoData.splice(20, aoData.length - 20);
                                                                          }

                                                                      });



        that.tierFilter.change(function () {

            var tableSettings = attachmentTable.fnSettings();
            tableSettings.sAjaxSource = loadAttachmentUrl + '?categoryId=' +  $("#tierFilter").val();
            attachmentTable.fnDraw();
        });

        $("#uploadAttachment").click(function () {
            $(that.yesButton).addClass("hidden");
            $(that.noButton).addClass("hidden");
            that.errorMessages.text("");
            var lineItems = that.numberofSelectedLineItems(that);
            if ($("#tierFilter").val() == "ServiceAssurance"  && lineItems != 1 ) {
                that.userMessage.html('Please select one and only one site');
                that.userMessage.removeClass("hidden");
            }
            var attachmentErrorClass = that.userMessage.hasClass("hidden");
            if (attachmentErrorClass) {
            that.progressDialog.taskStarted("Uploading ...");
            that.progressDialogInstance.open();
            that.errorMessages.addClass("hidden");


            function completed(header, errorMessages) {
                if (errorMessages.length === 0) {
                    that.progressDialog.taskFinished(header);
                } else {
                    that.progressDialog.taskFinishedWithErrors(header);
                    that.errorMessages.text(errorMessages).removeClass("hidden");
                }
            }
            function getFilenameWithoutExtension(fileName){
                var endIndex = fileName.lastIndexOf(".");
                return fileName == ""?"":endIndex!=-1?fileName.substring(0, endIndex):fileName;
            }
            function isFileNameValid(fileName){
                if(fileName == "" || fileName.length >= 66) {
                    return false;
                }
                if (!/^[^\^\?\"\<\>\|\~\(\)\%\&\*\:\/\\{\}\']+$/.test(fileName)) {
                    return false;
                }
                return true;
            }
            var error = function (response) {
                completed("Upload unsuccessful, please try again.", "");
            };

            var filePath=$("#attachmentName").val();
            var fileName = filePath.split('\\').pop().split('/').pop();
            var fileNameWithoutExtn = getFilenameWithoutExtension(fileName);
            if(!isFileNameValid(fileNameWithoutExtn)){
                completed("Upload unsuccessful, please try again.", "File name cannot contain special characters (^ ? \" < > | ~ ( ) % & * : / \\ { }) Please remove these from the file name and upload once again.");
            } else {
                var uploadAttachmentUrl = $("#uploadAttachmentActionUrl").val() + "?categoryId=" + $("#tierFilter").val() + "&fileName=" + fileName;

                if (that.errorMessages.text() == "") {
                    $("#AttachmentDialogForm").ajaxSubmit({
                                                              url:uploadAttachmentUrl,
                                                              success:function (data) {
                                                                  var attachmentUploadResponse = $.parseJSON(data);
                                                                  if (attachmentUploadResponse.successful) {
                                                                      completed("Upload Successful", "");
                                                                      $(that.attachmentDataTableId).dataTable().fnDraw(true)
                                                                  }
                                                                  else {
                                                                      var returnedErrors = attachmentUploadResponse.errors;
                                                                      completed("Upload unsuccessful, please try again.", returnedErrors);
                                                                  }
                                                              },
                                                              error:error
                                                          });
                }
            }
            }
        });

        $(that.cancelButtonId).click(function() {
            that.cancelHandler();
        });
    },

    fnAttachmentRowCallback:function (row, aData) {
        var that = this;
        var downloadAttachmentUrl = $("#downloadAttachmentActionUrl").val() +
                                    "?categoryId="+ aData.uploadAppliesTo;

        $.each($("td", row), function (columnIndex, td) {

            var colName = that.columnMetaData[columnIndex].mDataProp;
            $(td).addClass(colName);

            switch(columnIndex) {
                case 0:
                    $(td).attr("id", aData.uploadAppliesTo);
                    break;
                case 1:
                    $(td).attr("id", aData.uploadFileName);
                    $(td).html("<a class='configure action' href='" + downloadAttachmentUrl + "&fileName="+ aData.uploadFileName + "&documentId=" + aData.id + "' target='"+"'_self'" + aData.uploadFileName + "'>" + aData.uploadFileName + "</a>");
                    break;
                case 2:
                    $(td).attr("id", aData.uploadDate);
                    break;
                case 3:
                    $(td).html("<a class='delete' href='#'><img src='/rsqe/project-engine/static/images/recyclebin_empty.png' title='Delete Attachment' alt='Delete Attachment'/></a>");
                    break;
            }
        });

        $(row).attr("id", aData.id);
        $(row).addClass("attachmentDataTable");
        $(row).attr("categoryId", aData.uploadAppliesTo);
        $(row).attr("filename", aData.uploadFileName);
        return row;
    },

    numberofSelectedLineItems:function (that) {
        var selectedLineItems = 0;
        $('input[type=checkbox]', that.lineItemsTable).each(function () {
            if (this.checked) {
                selectedLineItems++;
            }
        });
        return selectedLineItems;
    },

    bindDeleteClick:function () {
        var that = this;
        $(".delete").click(function () {
            $(that.progressButton).addClass("hidden");
            var row = $(this).parents("tr:first");
            that.errorMessages.text("");
            var deleteAttachmentUrl = $("#deleteAttachmentActionUrl").val() +
                                      "?categoryId=" + row.attr("categoryId") +
                                      "&documentId=" + row.attr("id");

            that.progressDialog.taskDelete("You are about to delete an attachment from this Order, Are you sure?");
            that.progressDialogInstance.open();

            $(that.noButtonId).click(function() {
                $("#progressDialog").dialog("close");
            });

            $(that.yesButtonId).click(function() {
                that.progressDialog.taskDelete("Deleting ...");
                $(that.yesButtonId).addClass("hidden");
                $(that.noButtonId).addClass("hidden");

                function completed(header, errorMessages) {
                    if (errorMessages.length === 0) {
                        that.progressDialog.taskFinished(header);
                    } else {
                        that.progressDialog.taskFinishedWithErrors(header);
                        that.errorMessages.text(errorMessages).removeClass("hidden");
                    }
                }

                var error = function () {
                    completed("Delete unsuccessful, please try again.", "");
                };

                $("#AttachmentDialogForm").ajaxSubmit({
                                                          url:deleteAttachmentUrl,
                                                          success:function (data) {
                                                              var attachmentUploadResponse = $.parseJSON(data);
                                                              if (attachmentUploadResponse.successful) {
                                                                  completed("Delete Successful", "");
                                                                  $(that.attachmentDataTableId).dataTable().fnDraw(true)
                                                              }
                                                              else {
                                                                  var returnedErrors = attachmentUploadResponse.errors;
                                                                  completed("Delete unsuccessful, please try again.", returnedErrors);
                                                              }
                                                          },
                                                          error:error
                                                      });

            });
        });
    },
    setupProgressDialog:function () {
        var that = this;
        that.progressDialogInstance = new rsqe.Dialog(that.progressDialogContainer, {
            title:"Progress",
            width:"350px",
            closers:that.progressDialogContainer.find(".close")
        });
    },
    resetForm: function() {
        this.attachmentDataTableId.clearForm();
        this.userMessage.addClass("hidden");
        this.errorMessages.addClass("hidden");
    }
};
