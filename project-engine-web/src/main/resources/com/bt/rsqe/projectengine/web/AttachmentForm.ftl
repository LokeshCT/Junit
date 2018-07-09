<@cc.dialog id="AttachmentDialog"  ; for>
    <@content for=="main">
    <form id="AttachmentDialogForm" action="#" enctype="multipart/form-data" method="post">
        <div id="message" class="error hidden"></div>
        <label>Attachment Applies to:</label>
        <@cc.select id="tierFilter">
            <#list view.tierList as tier>
                <option value="${tier}">${tier}</option>
            </#list>
        </@cc.select>

        <br/><br/>
        <label>Select Files to Upload:</label>
        <input type="file" id="attachmentName" name="attachmentName" class="attachmentName"/>
        <input type="button" id="uploadAttachment" value="Upload" class="submit" name="uploadAttachment"/>
        <div class="data-Container border">
            <table id="attachmentTableItems">
                <thead>
                <tr>
                    <th>Attachment Applies To</th>
                    <th>File Name</th>
                    <th>Uploaded Date (GMT)</th>
                    <th>Delete</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>

        <input type="hidden" name="loadAttachmentActionUrl" id="loadAttachmentActionUrl" value="${view.loadAttachmentUri}"/>
        <input type="hidden" name="uploadAttachmentActionUrl" id="uploadAttachmentActionUrl" value="${view.uploadAttachmentUri}"/>
        <input type="hidden" name="downloadAttachmentActionUrl" id="downloadAttachmentActionUrl" value="${view.downloadAttachmentUri}"/>
        <input type="hidden" name="deleteAttachmentActionUrl" id="deleteAttachmentActionUrl" value="${view.deleteAttachmentUri}"/>
    </form>
    </@content>
</@cc.dialog>
