package com.concur.mobile.core.travel.data;

import java.io.Serializable;
import java.util.List;

/**
 * Cancellation policy - Travel specific segments in future can extend this class
 * 
 * @author RatanK
 * 
 */
public class CancellationPolicy implements Serializable {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -5118796388609789317L;

    private List<String> statements;

    public List<String> getStatements() {
        return statements;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }

}
