/**
 * 
 * Extension of concur service handle class to handle server client communication
 * */
package com.concur.mobile.gov.service;

import java.io.IOException;

import org.apache.http.HttpStatus;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ConcurServiceHandler;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.travel.air.service.AirFilterReply;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.charge.service.AddToVchReply;
import com.concur.mobile.gov.expense.charge.service.AddToVchRequest;
import com.concur.mobile.gov.expense.charge.service.DeleteTMUnappliedExpenseRequest;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListReply;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListRequest;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.service.AttachTMReceiptReply;
import com.concur.mobile.gov.expense.doc.service.AttachTMReceiptRequest;
import com.concur.mobile.gov.expense.doc.service.DeleteTMExpenseRequest;
import com.concur.mobile.gov.expense.doc.service.DocumentDetailReply;
import com.concur.mobile.gov.expense.doc.service.DocumentDetailRequest;
import com.concur.mobile.gov.expense.doc.service.DocumentListReply;
import com.concur.mobile.gov.expense.doc.service.DocumentListRequest;
import com.concur.mobile.gov.expense.doc.service.GetAuthForVchDocListRequest;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseFormReply;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseFormRequest;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseTypesReply;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseTypesRequest;
import com.concur.mobile.gov.expense.doc.service.SaveTMExpenseFormReply;
import com.concur.mobile.gov.expense.doc.service.SaveTMExpenseFormRequest;
import com.concur.mobile.gov.expense.doc.stamp.data.ReasonCodeReqdResponse;
import com.concur.mobile.gov.expense.doc.stamp.service.AvailableStampsRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.DsStampReply;
import com.concur.mobile.gov.expense.doc.stamp.service.StampRequirementInfoReply;
import com.concur.mobile.gov.expense.doc.stamp.service.StampRequirementInfoRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.StampTMDocumentRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.StampTMDocumentResponse;
import com.concur.mobile.gov.expense.doc.voucher.service.CreateVoucherFromAuthRequest;
import com.concur.mobile.gov.expense.service.GovSearchListRequest;
import com.concur.mobile.gov.expense.service.GovSearchListResponse;
import com.concur.mobile.gov.travel.service.AuthNumsReply;
import com.concur.mobile.gov.travel.service.AuthNumsRequest;
import com.concur.mobile.gov.travel.service.DocInfoFromTripLocatorReply;
import com.concur.mobile.gov.travel.service.DocInfoFromTripLocatorRequest;
import com.concur.mobile.gov.travel.service.GovAirFilterRequest;
import com.concur.mobile.gov.travel.service.GovAirSearchReply;
import com.concur.mobile.gov.travel.service.GovAirSearchRequest;
import com.concur.mobile.gov.travel.service.GovAirSellReply;
import com.concur.mobile.gov.travel.service.GovAirSellRequest;
import com.concur.mobile.gov.travel.service.GovCarSellReply;
import com.concur.mobile.gov.travel.service.GovCarSellRequest;
import com.concur.mobile.gov.travel.service.GovHotelConfirmReply;
import com.concur.mobile.gov.travel.service.GovHotelConfirmRequest;
import com.concur.mobile.gov.travel.service.GovHotelSearchRequest;
import com.concur.mobile.gov.travel.service.GovRailSellReply;
import com.concur.mobile.gov.travel.service.GovRailSellRequest;
import com.concur.mobile.gov.travel.service.PerDiemLocationListReply;
import com.concur.mobile.gov.travel.service.PerDiemLocationListRequest;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.travel.service.PerDiemRateRequest;

public class GovServiceHandler extends ConcurServiceHandler {

    private static final String CLS_TAG = GovServiceHandler.class.getSimpleName();

    protected GovServiceHandler() {
    }

    /**
     * 
     * @param service
     *            A reference to the running {@link ConcurService}
     * @param looper
     *            A reference to the {@link Looper} attached to this thread
     */
    public GovServiceHandler(GovService service, Looper looper) {
        super(service, looper);
    }

    @Override
    public void handleMessage(Message msg) {

        if (!verifySession(msg)) {
            return;
        }

        switch (msg.what) {
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_DOCUMENT: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_documents).toString());
                // retrieve request form handler message
                DocumentListRequest request = (DocumentListRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_DOCUMENT);
                try {
                    // process request with govConcurService
                    DocumentListReply documentListReply = (DocumentListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, documentListReply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, documentListReply.httpStatusText);
                    if (documentListReply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, documentListReply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (documentListReply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setDocumentListReply(documentListReply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, documentListReply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_DOCUMENT_DETAIL: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_documents_detail).toString());
                // retrieve request form handler message
                DocumentDetailRequest request = (DocumentDetailRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_DOCUMENT_DETAIL);
                try {
                    // process request with govConcurService
                    DocumentDetailReply docDetailReply = (DocumentDetailReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, docDetailReply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, docDetailReply.httpStatusText);
                    if (docDetailReply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, docDetailReply.mwsStatus);
                        if (docDetailReply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            DsDocDetailInfo info = docDetailReply.detailInfo;
                            if (info != null) {
                                MobileDatabase mdb = concurService.getMobileDatabase();
                                Cursor cursor = mdb
                                    .loadGovDocument(info.userID, info.travelerId, info.documentName, info.docType);
                                if (cursor.getCount() > 0) {
                                    if (cursor.moveToFirst()) {
                                        DsDocDetailInfo replyFromCursor = new DsDocDetailInfo(cursor);
                                        if (info.userID.equalsIgnoreCase(replyFromCursor.userID)
                                            && info.travelerId.equalsIgnoreCase(replyFromCursor.travelerId)
                                            && info.documentName.equalsIgnoreCase(replyFromCursor.documentName)
                                            && info.docType.equalsIgnoreCase(replyFromCursor.docType)) {
                                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                                            app.setDocDetailInfo(replyFromCursor);
                                        }
                                    }
                                }
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, docDetailReply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_LIST_OF_STAMP: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_document_stamp_list).toString());
                // retrieve request form handler message
                AvailableStampsRequest request = (AvailableStampsRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_LIST_OF_STAMP);
                try {
                    // process request with govConcurService
                    DsStampReply stampReply = (DsStampReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, stampReply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, stampReply.httpStatusText);
                    if (stampReply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, stampReply.mwsStatus);
                        if (stampReply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                            app.stampCache.setStampReply(stampReply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, stampReply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_STAMP_REQ_RESPONSE: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_documents_stamp_reqinfo).toString());
                // retrieve request form handler message
                StampRequirementInfoRequest request = (StampRequirementInfoRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_STAMP_REQ_INFO);
                try {
                    // process request with govConcurService
                    StampRequirementInfoReply reply = (StampRequirementInfoReply) request
                        .process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            ReasonCodeReqdResponse infoReq = reply.reqdResponse;
                            if (infoReq != null) {
                                MobileDatabase mdb = concurService.getMobileDatabase();
                                Cursor cursor = mdb
                                    .loadStampDocumentRequirementInfo(request.stampReqUserId, request.travId, infoReq.stampName);
                                if (cursor.getCount() > 0) {
                                    if (cursor.moveToFirst()) {
                                        ReasonCodeReqdResponse replyFromCursor = new ReasonCodeReqdResponse(cursor);
                                        if (infoReq.stampReqUserId.equalsIgnoreCase(replyFromCursor.stampReqUserId)
                                            && infoReq.travId.equalsIgnoreCase(replyFromCursor.travId)
                                            && infoReq.stampName.equalsIgnoreCase(replyFromCursor.stampName)) {
                                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                                            app.stampCache.setStampReqRes(replyFromCursor);
                                        }
                                    }
                                }
                            } else {
                                Log.e(CLS_TAG, "StampRequirementInfoReply.requiredinfo = null");
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_STAMP_DOC_RESPONSE: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_stamp_document_progress).toString());
                // retrieve request form handler message
                StampTMDocumentRequest request = (StampTMDocumentRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_STAMP_DOC);
                try {
                    // process request with govConcurService
                    StampTMDocumentResponse reply = (StampTMDocumentResponse) request
                        .process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                            app.stampCache.setStampTMDocumentResponse(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_UNAPP_EXP_LIST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_unapplied_expenses).toString());
                // retrieve request form handler message
                MobileExpenseListRequest request = (MobileExpenseListRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_UNAPP_EXPENSE);
                try {
                    // process request with govConcurService
                    MobileExpenseListReply reply = (MobileExpenseListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.vchCache.setMobileExpenseListReply(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_CREATE_VOUCHER_FROM_AUTH: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_create_voucher).toString());

                CreateVoucherFromAuthRequest request = (CreateVoucherFromAuthRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_VOUCHER_CREATE_FROM_AUTH);
                DocumentListReply reply = null;
                try {
                    // process request with govConcurService
                    reply = (DocumentListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                            if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                // update cache..
                                // Although we don't really care for this one because we will
                                // immediately refetch the voucher list when this completes
                                app.setDocumentListReply(reply);
                                app.setDocumentListRefReq(true);
                            } else {
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }

                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_ADD_EXP_TO_VCH: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_add_to_vch_progress).toString());

                AddToVchRequest request = (AddToVchRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_ADD_TO_VCH_EXP);
                AddToVchReply reply = null;
                try {
                    // process request with govConcurService
                    reply = (AddToVchReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                            app.vchCache.setAddToVchReply(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GOV_MSGS: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_msgs).toString());
                // retrieve request form handler message
                GovMessagesRequest request = (GovMessagesRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GOV_MSGS);
                try {
                    // process request with govConcurService
                    GovMessagesReply reply = (GovMessagesReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // set gov messages to application.
                            app.setMsgs(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_ATTACH_TM_RECEIPT: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.upload_receipt).toString());

                AttachTMReceiptRequest request = (AttachTMReceiptRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_ATTACH_TM_RECEIPT);
                AttachTMReceiptReply reply = null;
                try {
                    // process request with govConcurService
                    reply = (AttachTMReceiptReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_STATUS, reply.message);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }

                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_AUTH_NUMS: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_get_auth_nums).toString());
                // retrieve request form handler message
                AuthNumsRequest request = (AuthNumsRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_AUTH_NUMS);
                try {
                    // process request with govConcurService
                    AuthNumsReply reply = (AuthNumsReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // store reply to cache.
                            app.trvlBookingCache.setAuthNumsReply(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_PERDIEM_LOCATION: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_get_per_diem_location).toString());
                // retrieve request form handler message
                PerDiemLocationListRequest request = (PerDiemLocationListRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_PERDIEM_LOCATIONS);
                try {
                    // process request with govConcurService
                    PerDiemLocationListReply reply = (PerDiemLocationListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // store reply to cache.
                            app.trvlBookingCache.setPerDiemLocationListReply(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_PERDIEM_RATE_LOCATION: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_get_per_diem_rate_location).toString());
                // retrieve request form handler message
                PerDiemRateRequest request = (PerDiemRateRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_PERDIEM_RATE_LOCATIONS);
                try {
                    // process request with govConcurService
                    PerDiemRateReply reply = (PerDiemRateReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // store reply to cache.
                            app.trvlBookingCache.setPerDiemRateReply(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case Const.MSG_TRAVEL_HOTEL_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.searching_for_hotels).toString());

                GovHotelSearchRequest request = (GovHotelSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_HOTEL_SEARCH_RESULTS);
                try {
                    HotelSearchReply reply = (HotelSearchReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            HotelSearchReply curReply = app.getHotelSearchResults();
                            if (curReply != null) {
                                curReply.hotelChoices.addAll(reply.hotelChoices);
                                curReply.length += reply.length;
                            } else {
                                app.setHotelSearchResults(reply);
                            }
                            // MOB-12309
                            app.clearHotelDetailCache();
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setHotelSearchResults(null);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_SEND_AGREEMENT: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_sending_agreement).toString());
                GovRulesAgreementRequest request = (GovRulesAgreementRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_SEND_AGREEMENT);
                try {
                    ServiceReply reply = request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SendAgreement): MWS status("
                                + reply.mwsStatus
                                + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SendAgreement): HTTP status("
                            + reply.httpStatusCode
                            + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case Const.MSG_TRAVEL_AIR_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.searching_for_flights)
                    .toString());

                GovAirSearchRequest request = (GovAirSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_AIR_SEARCH_RESULTS);

                try {
                    GovAirSearchReply reply = (GovAirSearchReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setAirSearchResults(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setAirSearchResults(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }

        case Const.MSG_TRAVEL_AIR_FILTER_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.filtering_flights).toString());

                GovAirFilterRequest request = (GovAirFilterRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_AIR_FILTER_RESULTS);

                try {
                    AirFilterReply reply = (AirFilterReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setAirFilterResults(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setAirSearchResults(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_AIR_SELL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.booking_air).toString());
                GovAirSellRequest request = (GovAirSellRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_AIR_BOOK_RESULTS);

                try {
                    GovAirSellReply reply = (GovAirSellReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, Long.parseLong(reply.itinLocator));
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AIR_AUTHORIZATION_NUM, reply.authorizationNumber);
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AIR_TRIP_LOCATOR, reply.tripLocator);
                        } else {
                            if (reply.mwsErrorMessage != null) {
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            } else {
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.errorMsg);
                            }
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_TRAVEL_HOTEL_CONFIRM_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.confirming_hotel_room)
                    .toString());

                GovHotelConfirmRequest request = (GovHotelConfirmRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_HOTEL_CONFIRM_RESULTS);
                try {
                    GovHotelConfirmReply reply = (GovHotelConfirmReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, Long.parseLong(reply.itinLocator));
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AIR_AUTHORIZATION_NUM, reply.authorizationNumber);
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AIR_TRIP_LOCATOR, reply.tripLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }

        case Const.MSG_TRAVEL_CAR_SELL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.booking_car).toString());

                GovCarSellRequest request = (GovCarSellRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_CAR_SELL_RESULTS);
                try {
                    GovCarSellReply reply = (GovCarSellReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, Long.parseLong(reply.itinLocator));
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AIR_AUTHORIZATION_NUM, reply.authorizationNumber);
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AIR_TRIP_LOCATOR, reply.tripLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }

        case Const.MSG_TRAVEL_RAIL_SELL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.booking_train).toString());

                GovRailSellRequest request = (GovRailSellRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_RAIL_SELL_RESULTS);
                try {
                    GovRailSellReply reply = (GovRailSellReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AIR_AUTHORIZATION_NUM, reply.authorizationNumber);
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, reply.itinLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }

                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_documents).toString());
                // retrieve request form handler message
                DocInfoFromTripLocatorRequest request = (DocInfoFromTripLocatorRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_DOCUMENT);
                try {
                    // process request with govConcurService
                    DocInfoFromTripLocatorReply documentListReply = (DocInfoFromTripLocatorReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, documentListReply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, documentListReply.httpStatusText);
                    if (documentListReply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, documentListReply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (documentListReply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.trvlBookingCache.setDocumentListReply(documentListReply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, documentListReply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_TM_EXPENSE_TYPES: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_retrieve_expense_types).toString());

                GetTMExpenseTypesRequest request = (GetTMExpenseTypesRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_TM_EXPENSE_TYPES);
                GetTMExpenseTypesReply reply = null;
                try {
                    // process request with govConcurService
                    reply = (GetTMExpenseTypesReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                            app.setExpenseTypes(reply.expenseTypes);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }

                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_TM_EXPENSE_FORM: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_retrieve_expense_form).toString());

                GetTMExpenseFormRequest request = (GetTMExpenseFormRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_TM_EXPENSE_FORM);
                GetTMExpenseFormReply reply = null;
                try {
                    // process request with govConcurService
                    reply = (GetTMExpenseFormReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            GovAppMobile app = (GovAppMobile) concurService.getApplication();
                            app.setCurrentExpenseForm(reply.form);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }

                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_DELETE_TM_EXPENSE: {

            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_delete_doc_expense).toString());

                DeleteTMExpenseRequest request = (DeleteTMExpenseRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_DOC_EXPENSE_DELETED);
                try {
                    ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {

                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);

                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            // Be sure to delete the Expense from both the cache and DB.
                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                            DsDocDetailInfo docDetailInfo = app.getDocDetailInfo();
                            if (docDetailInfo != null
                                && docDetailInfo.userID.equals(request.userId)
                                && docDetailInfo.documentName.equals(request.docName)
                                && docDetailInfo.docType.equals(request.docType)) {

                                if (docDetailInfo.deleteExpense(request.expId)) {

                                    ContentValues contentValues = DsDocDetailInfo.getContentVals(docDetailInfo);
                                    MobileDatabase mdb = concurService.getMobileDatabase();

                                    if (!(mdb.insertGovDocument(docDetailInfo.userID,
                                        docDetailInfo.travelerId,
                                        docDetailInfo.documentName,
                                        docDetailInfo.docType,
                                        contentValues))) {

                                        Log.e(Const.LOG_TAG, CLS_TAG
                                            + " .processResponse: DsDocDetailInfo insertion is failed");
                                    }

                                    // Update the cache (global reference).
                                    app.setDocDetailInfo(docDetailInfo);
                                }
                            }

                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DeleteMobileEntries): HTTP status("
                            + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_DELETE_TM_UNAPPLIED_EXPENSE: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_delete_unapplied_expense).toString());

                DeleteTMUnappliedExpenseRequest request = (DeleteTMUnappliedExpenseRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_UNAPPLIED_EXPENSE_DELETED);
                try {
                    ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {

                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);

                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            // Be sure to delete the Expense the cache;
                            GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                            if (!app.vchCache.deleteUnappliedExpense(request.ccExpId)) {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                    + " .processResponse: Could not delete Unapplied Expense from cache with ccExpId: "
                                    + request.ccExpId);
                            }

                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DeleteMobileEntries): HTTP status("
                            + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case com.concur.mobile.gov.util.Const.HANDLER_MSG_SAVE_TM_EXPENSE_FORM: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.gov_saving_expense).toString());

                SaveTMExpenseFormRequest request = (SaveTMExpenseFormRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_SAVE_TM_EXPENSE_FORM);
                try {
                    SaveTMExpenseFormReply reply = (SaveTMExpenseFormReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {

                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);

                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.resultText);
                            intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_ID, reply.expId);
                            // TODO enable it when required.
                            // intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_DATE, reply.date);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveTMExpenseForm): HTTP status("
                            + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case Const.MSG_EXPENSE_SEARCH_LIST_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_search_list).toString());

                GovSearchListRequest request = (GovSearchListRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_SEARCH_LIST_UPDATED);
                try {
                    GovSearchListResponse reply = (GovSearchListResponse) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set the last search results on the application.
                            GovAppMobile app = (GovAppMobile) concurService.getApplication();
                            app.setGovSearchListResponse(reply);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SearchListRequest): MWS status("
                                + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SearchListRequest): HTTP status("
                            + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_AUTH_FOR_VCH_DOCUMENT: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService
                    .getText(R.string.gov_retrieve_auth_for_vch_documents).toString());
                // retrieve request form handler message
                GetAuthForVchDocListRequest request = (GetAuthForVchDocListRequest) msg.obj;
                Intent intent = new Intent(com.concur.mobile.gov.util.Const.ACTION_GET_AUTH_FOR_VCH_DOCUMENT);
                try {
                    // process request with govConcurService
                    DocumentListReply documentListReply = (DocumentListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, documentListReply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, documentListReply.httpStatusText);
                    if (documentListReply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, documentListReply.mwsStatus);
                        GovAppMobile app = ((GovAppMobile) concurService.getApplication());
                        if (documentListReply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setAuthForVchDocumentList(documentListReply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, documentListReply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent
                        .putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        default:
            super.handleMessage(msg);
            break;
        }
    }
}
