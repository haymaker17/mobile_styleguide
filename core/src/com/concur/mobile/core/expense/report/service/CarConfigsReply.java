package com.concur.mobile.core.expense.report.service;

import java.util.ArrayList;

import com.concur.mobile.core.expense.report.data.CarConfig;
import com.concur.mobile.core.service.ServiceReply;

public class CarConfigsReply extends ServiceReply {

    public ArrayList<CarConfig> carConfigs;

    public String xmlReply;
}
