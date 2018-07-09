package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.google.common.base.Joiner;

public class SupplierCheckRequestId {

    final static String SCAR = "APERQ";
    final static String SCCR = "CLNRQ";

    String requestId;

    public SupplierCheckRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String value() {
        return requestId;
    }

    public static SupplierCheckRequestId newInstance(String requestId) {
        return new SupplierCheckRequestId(requestId);
    }

    public static SupplierCheckRequestId forApe(final APEQrefJPARepository apeQrefRepository) throws Exception {
        Long nextSequence = apeQrefRepository.getNextValOfSupplierCheckApeRequestId();
        return SupplierCheckRequestId.newInstance(Joiner.on("").join(SCAR, nextSequence));
    }

    public static SupplierCheckRequestId forClient(final APEQrefJPARepository apeQrefRepository) throws Exception {
        Long nextSequence = apeQrefRepository.getNextValOfSupplierCheckClientRequestId();
        return SupplierCheckRequestId.newInstance(Joiner.on("").join(SCCR, nextSequence));
    }
}
