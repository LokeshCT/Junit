var form;
describe('Attachment Dialog', function () {
    var attachmentType, userMessage, attachmentTable;

    afterEach(function () {
        form.resetForm();
    });

    describe('When page loaded', function () {

        beforeEach(function () {
            spyOn($, "ajax").andCallFake(function (options) {
                options.success(sampleResponse);
            });

            form = new rsqe.AttachmentForm({cancelHandler:function () {}});
            form.setUpAttachmentDialog();

            attachmentType = $('#tierFilter');
            userMessage = $('#message');
            attachmentTable = $('#attachmentTableItems');
        });


        it('should set default attachment type to "Sales"', function () {
            expect(attachmentType.val()).toEqual('Sales');
            expect(userMessage.hasClass('hidden')).toBeTruthy();
        });

        it('should load table for Service Delivery option by default', function () {
            expect($('#attachmentTableItems tbody tr').length).toEqual(2);
        });

    });

    describe('When No attachment type selected', function () {

        beforeEach(function () {
            spyOn($, "ajax").andCallFake(function (options) {
                options.success({"itemDTOs":[]});
            });

            form = new rsqe.AttachmentForm({cancelHandler:function () {}});
            form.setUpAttachmentDialog();

            attachmentType = $('#tierFilter');
            userMessage = $('#message');
            attachmentTable = $('#attachmentTableItems');
        });

        it('should remove all rows when no attachment type selected', function () {
            attachmentType.val("--Please Select--");
            attachmentType.change();

            expect($('#attachmentTableItems tbody tr td')[0].innerHTML).toEqual('No attachments found for the selected category.');
        });


        it('should hide error when attachment type selected', function () {
            attachmentType.val("--Please Select--");
            attachmentType.change();

            attachmentType.val("ServiceDelivery");
            attachmentType.change();

            expect(userMessage.hasClass('hidden')).toBeTruthy()
        });
    });

    var sampleResponse = {"itemDTOs":[
        {
            "uploadAppliesTo":"ServiceDelivery",
            "uploadFileName":"DeliveryMultiplyOne.xls",
            "uploadDate":"18/03/1982",
            "id":"id1"
        },
        {
            "uploadAppliesTo":"ServiceDelivery",
            "uploadFileName":"DeliveryMultiplyTwo.xls",
            "uploadDate":"18/03/1982",
            "id":"id2"
        }
    ], "sEcho":"1", "iTotalDisplayRecords":"2", "iTotalRecords":"0"};

});
