//
//  GovDocStampVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocStampVC.h"
#import "GovDocumentManager.h"
#import "GovStampRequirementInfo.h"
#import "GovDocAvailableStampsData.h"
#import "GovDocStampInfo.h"
#import "GovDocReturnToInfo.h"
#import "GovDocReasonCode.h"
#import "GovStampRequirementInfoData.h"
#import "GovStampTMDocumentData.h"
#import "GovDocumentListVC.h"

@interface GovDocStampVC (Private)
-(BOOL) hasAllData;
-(BOOL) signatureRequired;
-(BOOL) reasonRequired;
-(BOOL) returnToRequired;
@end

@implementation GovDocStampVC
@synthesize lblAwaitingStatus, awaitingStatus, availStamps, requiresReason, docName, docType, travelerId, selectedStamp, reasonCodes;
@synthesize delegate = _delegate;

-(void) actionClose:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void) setupToolbar
{
    [self updateSaveBtn];
    
    if([UIDevice isPad])
    {
        //self.contentSizeForViewInPopover = CGSizeMake(320.0, 360.0);
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionClose:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }
}


#pragma mark - Init data
- (void)setSeedData:(GovDocumentDetail*)doc
{
    self.docName = doc.docName;
    self.docType = doc.docType;
    self.travelerId = doc.travelerId;
    self.reasonCodes = doc.reasonCodes;
    
    EntityGovDocument* docListInfo = [[GovDocumentManager sharedInstance] fetchDocumentByDocName:self.docName withType:self.docType withTravelerId:self.travelerId];
    if (docListInfo != nil)
    {
        self.awaitingStatus = docListInfo.approveLabel;
    }
    
    self.selectedStamp = self.awaitingStatus;
    
    [self retrieveStampReasonRequired:self.selectedStamp];
    [self retrieveDocAvailStamps];
    [self initFields];
}

#pragma mark - Seed
-(void) setSeedDelegate:(id<GovDocStampVCDelegate>)del
{
    self.delegate = del;
}

- (void)viewDidLoad
{
    self.noSaveConfirmationUponExit = true;
    
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.title = [@"Stamp Document" localize];
    self.lblAwaitingStatus.text = self.awaitingStatus;
    [self setupToolbar];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma messages
-(void) retrieveStampReasonRequired:(NSString*) stampName
{
    // Check if requiresReason is set
    self.requiresReason = [GovStampRequirementInfo requiresReason:stampName];
    if (requiresReason == nil && [stampName length])
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     stampName, @"STAMP_NAME",
                                     nil];
        
        [[ExSystem sharedInstance].msgControl createMsg:GOV_STAMP_REQ_INFO CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

-(void) retrieveDocAvailStamps
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 self.travelerId, @"TRAVELER_ID",
								 self.docType, @"DOC_TYPE",
								 self.docName, @"DOC_NAME",
								 nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_DOC_AVAIL_STAMPS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) stampDocument
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 self.travelerId, @"TRAVELER_ID",
								 self.docType, @"DOC_TYPE",
								 self.docName, @"DOC_NAME",
                                 self.selectedStamp, @"STAMP_NAME",
                                 nil];
    
    if ([self signatureRequired])
    {
        FormFieldData *sigFld = [self findEditingField:@"SigningPin"];
        [pBag setObject:sigFld.fieldValue forKey:@"SIG_KEY"];

        FormFieldData *commentFld = [self findEditingField:@"CommentPlain"];
        [pBag setObject:commentFld.fieldValue forKey:@"COMMENTS"];
    }
    
    if ([self reasonRequired])
    {
        FormFieldData *reasonFld = [self findEditingField:@"Reason"];
        [pBag setObject:reasonFld.liKey forKey:@"REASON_CODE"];
    }
    
    if ([self returnToRequired])
    {
        FormFieldData *returnToFld = [self findEditingField:@"ReturnTo"];
        [pBag setObject:returnToFld.liKey forKey:@"RETURN_TO"];
    }

    [[ExSystem sharedInstance].msgControl createMsg:GOV_STAMP_TM_DOCUMENTS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:GOV_DOC_AVAIL_STAMPS])
    {
        if (msg.errBody != nil)
        {
            NSString* errCode = [Localizer getLocalizedText:@"Failed to get stamp information"];
            
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:errCode
                                  message:msg.errBody
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            [alert show];
        }
        else
        {
            GovDocAvailableStampsData* asData = (GovDocAvailableStampsData*)msg.responder;
            self.availStamps = asData.availStamps;
            [self updateFields];
        }
    }
    else if ([msg.idKey isEqualToString:GOV_STAMP_REQ_INFO])
    {
        if (msg.errBody != nil)
        {
            NSString* errTitle = [Localizer getLocalizedText:@"Failed to get stamp requirement information"];
            
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:errTitle
                                  message:msg.errBody
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            [alert show];
        }
        else
        {
            if (self.requiresReason == nil)
                self.requiresReason = [GovStampRequirementInfo requiresReason:self.selectedStamp];
            [self updateFields];
        }
    }
    else if ([msg.idKey isEqualToString:GOV_STAMP_TM_DOCUMENTS])
    {
        if ([self isViewLoaded])
            [self hideWaitView];

        GovStampTMDocumentData* sdData = (GovStampTMDocumentData*) msg.responder;
        NSString* errMsg = msg.errBody != nil ? msg.errBody : nil;
        if (errMsg == nil)
            errMsg = sdData.status.errMsg;
        
        //Flurry stamp with Pin, or stamp without Pin
        NSMutableDictionary *param = [NSMutableDictionary dictionary];
        param[@"DocType"] = docType;
        param[@"Status Applied"] = self.selectedStamp;
        
        if (errMsg != nil)
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Failed to stamp document"]
                                  message:errMsg
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            [alert show];
            param[@"Action Server Status"] = @"Fail";
        }
        else
        {
            if(![UIDevice isPad])
            {
                UINavigationController* navController = self.navigationController;
                [navController popViewControllerAnimated:NO]; // Pop the stamp vc
                [navController popViewControllerAnimated:YES];// Pop the doc detail vc

                //Stamp 
                UIViewController *docListVC = navController.topViewController;
                if ([docListVC isKindOfClass:[GovDocumentListVC class]])
                {
                    GovDocumentListVC *lvc = (GovDocumentListVC *)docListVC;
                    [lvc refreshView:lvc.refreshControl];
                }
            }
            else
            {
                [self.delegate didStampSelectedDocument];
            }
            param [@"Action Server Status"] = @"Success";
        }
        
        if ([self signatureRequired])
        {
            [Flurry logEvent:@"Stamp Document: Stamp with pin" withParameters:param];
        }
        else
            [Flurry logEvent:@"Stamp Document: Stamp without pin" withParameters:param];
    }
    
}

#pragma mark - FormViewControllerBase helper
-(BOOL) hasAllData
{
    return self.availStamps != nil && self.requiresReason != nil;
}

-(BOOL) signatureRequired
{
    return self.availStamps.sigRequired != nil && [self.availStamps.sigRequired boolValue];
}

-(BOOL) reasonRequired
{
    return self.requiresReason != nil && [self.requiresReason boolValue];
}
-(BOOL) returnToRequired
{
    GovDocStampInfo *stampInfo = [self.availStamps.stampInfoList objectForKey:self.selectedStamp];
    return stampInfo != nil && stampInfo.returnToRequired != nil && [stampInfo.returnToRequired boolValue];
}

-(void) initFields
{
    self.sections = [[NSMutableArray alloc] initWithObjects:@"0", nil];
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    
    self.allFields = [[NSMutableArray alloc] initWithObjects: nil];
    
    FormFieldData* field= nil;
    field = [[FormFieldData alloc] initField:@"Stamp" label:[Localizer getLocalizedText:@"Stamp"] value:self.selectedStamp ctrlType:@"picklist" dataType:@"VARCHAR"];
    field.required = @"Y";
    [allFields addObject:field];
    
    if ([self hasAllData])
    {
        if ([self signatureRequired])
        {
            field = [[FormFieldData alloc] initField:@"SigningPin" label:[Localizer getLocalizedText:@"Signing Pin"] value:@"" ctrlType:@"edit" dataType:@"PASSWORD"];
            field.required = @"Y";
            [allFields addObject:field];

            field = [[FormFieldData alloc] initField:@"CommentPlain" label:[Localizer getLocalizedText:@"Comment"] value:@"" ctrlType:@"textarea" dataType:@"VARCHAR"];
            [allFields addObject:field];
        }
        
        if ([self reasonRequired])
        {
            field = [[FormFieldData alloc] initField:@"Reason" label:[Localizer getLocalizedText:@"Reason"] value:@"" ctrlType:@"picklist" dataType:@"VARCHAR"];
            field.required = @"Y";
            [allFields addObject:field];
        }

        if ([self returnToRequired])
        {
            field = [[FormFieldData alloc] initField:@"ReturnTo" label:[Localizer getLocalizedText:@"Return To"] value:@"" ctrlType:@"picklist" dataType:@"VARCHAR"];
            field.required = @"Y";
            [allFields addObject:field];
        }
    }

    [sectionFieldsMap setObject:allFields forKey:@"0"];
    [super initFields];
}

-(void)updateFields
{
    if (self.availStamps != nil && self.requiresReason != nil)
    {
        [self initFields];
        [self.tableList reloadData];
        [self hideLoadingView];
    }
}

//MOB-14117 check for required field before stamp a doc.
- (BOOL)validateFields:(BOOL *)missingReqFlds
{
    // make sure the reson code is selected before stamping
    BOOL result = [super validateFields:missingReqFlds];
    
    if ([self reasonRequired])
    {
        FormFieldData *reasonFld = [self findEditingField:@"Reason"];
        if (reasonFld.liKey == nil)
            result = FALSE;
    }
    
    if ([self returnToRequired])
    {
        FormFieldData *returnToFld = [self findEditingField:@"ReturnTo"];
        if (returnToFld.liKey == nil)
            result = FALSE;
    }
    
    if ([self signatureRequired])
    {
        FormFieldData *sigFld = [self findEditingField:@"SigningPin"];
        if (![sigFld.fieldValue lengthIgnoreWhitespace])
            result = FALSE;
    }
    
    return result;
}

-(void)actionStamp:(id)sender
{
    BOOL missingRequiredFields = NO;
	BOOL isFormValid = [self validateFields:&missingRequiredFields];
	[self.tableList reloadData];
	if (!isFormValid || missingRequiredFields)
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:nil
							  message:[Localizer getLocalizedText:@"STAMP_MISSING_REQ_FLD"]
							  delegate:nil
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles: nil];

        [alert show];
    }
    else
    {
        [self showWaitView];
        [self stampDocument];
    }
}

-(NSArray*) getStampListItems
{
    if (self.availStamps != nil)
    {
        NSMutableArray *result = [[NSMutableArray alloc] initWithCapacity:[self.availStamps.stampInfoList count]];
        
        for (GovDocStampInfo* stamp in self.availStamps.stampInfoList.allValues)
        {
            ListItem* item = [[ListItem alloc] init];
            item.liKey = stamp.stampName;
            item.liName = stamp.stampName;
            [result addObject:item];
        }
        return result;
    }
    else
        return nil;
}

-(NSArray*) getReturnToListItems
{
    if (self.availStamps != nil)
    {
        NSMutableArray *result = [[NSMutableArray alloc] initWithCapacity:[self.availStamps.returnToInfoList count]];
        
        for (GovDocReturnToInfo* returnTo in self.availStamps.returnToInfoList)
        {
            ListItem* item = [[ListItem alloc] init];
            item.liKey = returnTo.returnToId;
            item.liName = returnTo.returnToName;
            [result addObject:item];
        }
        return result;
    }
    else
        return nil;
}


-(NSArray*) getReasonListItems
{
    if (self.reasonCodes != nil)
    {
        NSMutableArray *result = [[NSMutableArray alloc] initWithCapacity:[self.reasonCodes count]];
        
        for (GovDocReasonCode* reason in self.reasonCodes)
        {
            ListItem* item = [[ListItem alloc] init];
            item.liKey = reason.code;
            item.liName = reason.comments;
            [result addObject:item];
        }
        return result;
    }
    else
        return nil;
}

#pragma mark - FormViewControllerBase override
-(void) fieldUpdated:(FormFieldData *)field
{
    if ([field.iD isEqualToString:@"Stamp"])
    {
        if (![self.selectedStamp isEqualToString:field.fieldValue])
        {
            self.selectedStamp = field.liKey;
            self.requiresReason = nil;
            [self showLoadingViewWithText:[Localizer getLocalizedText:@"Waiting"]];
            [self retrieveStampReasonRequired:self.selectedStamp];
            if (self.requiresReason != nil)
            {
                [self hideLoadingView];
                [self updateFields];
                return;
            }
        }
    }
    [super fieldUpdated:field];
}

-(void)prefetchForListEditor:(ListFieldEditVC*) lvc
{
    if (![lvc.field.iD isEqualToString:@"Stamp"] && ![lvc.field.iD isEqualToString:@"Reason"] && ![lvc.field.iD isEqualToString:@"ReturnTo"])
    {
        [super prefetchForListEditor:lvc];
        return;
    }
    
    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"MAIN_SECTION", nil];
    if ([lvc.field.iD isEqualToString:@"Stamp"])
    {
        // Prepopulate AtnTypeKey list
        lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getStampListItems], @"MAIN_SECTION", nil];
    }
    else if ([lvc.field.iD isEqualToString:@"Reason"])
    {
        lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getReasonListItems], @"MAIN_SECTION", nil];
    }
    else
    {
        lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getReturnToListItems], @"MAIN_SECTION", nil];
    }

    [lvc hideSearchBar];
}

-(void)updateSaveBtn
{
    NSMutableArray *toolbarItems = [NSMutableArray arrayWithCapacity:2];

    if(![ExSystem connectedToNetwork] || ![self canEdit])
    {
        [self setToolbarItems:toolbarItems animated:YES];
        return;
    }
    
	// Display Stamp button on nav bar, if not there already
    if ([self.toolbarItems count]<=1)
    {
        UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        [toolbarItems addObject:flexibleSpace];
        
        NSString* rightBtnLabel = [Localizer getLocalizedText:@"Stamp"];
        UIBarButtonItem *btnRight = [[UIBarButtonItem alloc] initWithTitle:rightBtnLabel style:UIBarButtonItemStyleBordered target:self action:@selector(actionStamp:)];
        [toolbarItems addObject:btnRight];
        
        [self.navigationController setToolbarHidden:NO];
        [self setToolbarItems:toolbarItems animated:YES];
    }
}

@end
