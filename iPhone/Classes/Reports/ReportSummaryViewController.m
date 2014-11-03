//
//  ReportSummaryViewController.m
//  ConcurMobile
//
//  Created by yiwen on 5/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportSummaryViewController.h"
#import "ViewConstants.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "SaveReportData.h"
#import "ReportDetailDataBase.h"
#import "ReportManager.h"
#import "EntityReport.h"
#import "iPadHomeVC.h"
#import "Config.h"
#import "ConditionalFieldsList.h"

#define kSectionCompanyName @"Company"
#define kSectionEmployeeName @"EMPLOYEE"
#define kSectionHeaderName @"Report Header"
#define kSectionExceptionsName @"Exceptions"

#define kGoToDetailAfterSaveNewReport 101921

@interface ReportSummaryViewController (Private)
-(void) recalculateSections;
- (void) refreshView;
-(BOOL) isNewReport;

@end

@implementation ReportSummaryViewController
@synthesize delegate = _delegate;

- (NSString*)getViewIDKey
{
    return APPROVE_REPORT_SUMMARY;    
}

- (void)setSeedData:(NSDictionary*)pBag
{
    [self setSeedData:pBag[@"REPORT"] role:pBag[@"ROLE"]];
}

- (void)setSeedData:(ReportData*)report role:(NSString*) curRole
{
    self.role = curRole;
    [self loadReport:report];
}

#pragma mark Save Methods
// Mob-10303 - override base class method for custom message
-(void) confirmToSave:(int) callerId
{
    // Alert to set required fields before save
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:nil
                          message:[Localizer getLocalizedText:@"RPT_QUIT_CONFIRM_MSG"]
                          delegate:self    
                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                          otherButtonTitles:[Localizer getLocalizedText:@"Yes"],[Localizer getLocalizedText:@"No"], nil];
    
    
    alert.tag = callerId;
    
    [alert show];
}


#pragma mark - View lifecycle
-(void)viewDidLoad
{
    [super viewDidLoad];
    if (![ExSystem connectedToNetwork] && (self.allFields == nil || self.allFields.count == 0))
    {
        [self showOfflineView:self];
        return;
    }
    
    if (self.allFields == nil)
        [self showLoadingView];
    
    if ([UIDevice isPad])
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionBack:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self refreshView];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
}

#pragma mark - Loading/Reloading
-(void) loadReport:(ReportData*) report
{
    // TODO - if formKey is different, we need to merge report
    if (self.rpt != nil && ![report.polKey isEqualToString:self.rpt.polKey])
        [self mergeFields:report.fields withKeys:report.fieldKeys];
    self.rpt = report;
    [self recalculateSections];
    [self refreshView];
}

- (void) refreshView
{
    if (rpt != nil && [self isViewLoaded])
    {
        [self drawHeaderRpt:rpt HeadLabel:lblName AmountLabel:lblAmount LabelLine1:lblLine1 LabelLine2:lblLine2 Image1:img1 Image2:img2 Image3:img3];
        
        [self setupToolbar];
        
        [tableList reloadData];
    }
}

-(void) initFields
{
	self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
	self.allFields = [[NSMutableArray alloc] init];
	
	NSMutableArray *fields = [[NSMutableArray alloc] init];
	
	NSMutableArray *fKeys = self.rpt.fieldKeys;
	for (NSString *fKey in fKeys)
	{
		FormFieldData* fld = (self.rpt.fields)[fKey];
		FormFieldData* fldForEdit = [fld copy];
		if ([fldForEdit.iD isEqualToString:@"PolKey"] && (fld.access == nil || [fld.access isEqualToString:@"RW"])&& ![self isNewReport])
		{
			fldForEdit.access = @"RO";
		}
        else if ([fldForEdit.ctrlType isEqualToString:@"hidden"])
        {
            fld.access = fldForEdit.access = @"HD";
        }
		if (fld.access == nil || ![fld.access isEqualToString:@"HD"])
			[fields addObject:fldForEdit];
		[self.allFields addObject:fldForEdit];
	}
	(self.sectionFieldsMap)[kSectionHeaderName] = fields;
    
    NSMutableArray* compFields = self.rpt.companyDisbursements;
    
    if (compFields != nil && [compFields count] >0)
    {
        FormFieldData* lastObj = (FormFieldData*) [compFields lastObject];
        if (lastObj != nil && ![lastObj.iD isEqualToString:@"CompTotal"])
        {
            lastObj = [[FormFieldData alloc] init];
            lastObj.iD = @"CompTotal";
            lastObj.label = [Localizer getLocalizedText:@"Total Paid by Company"];
            lastObj.fieldValue = self.rpt.totalPaidByCompany;
            lastObj.dataType = @"MONEY";
            lastObj.access = @"RO";
            [compFields addObject:lastObj];
        }
        (self.sectionFieldsMap)[kSectionCompanyName] = compFields;
    }
    
    NSMutableArray* empFields = self.rpt.employeeDisbursements;
    if (empFields != nil && [empFields count] >0)
    {
        FormFieldData* lastObj = (FormFieldData*) [empFields lastObject];
        if (lastObj != nil && ![lastObj.iD isEqualToString:@"EmpTotal"])
        {
            lastObj = [[FormFieldData alloc] init];
            lastObj.iD = @"EmpTotal";
            lastObj.label = [Localizer getLocalizedText:@"Total Owed by Employee"];
            lastObj.fieldValue = self.rpt.totalOwedByEmployee;
            lastObj.dataType = @"MONEY";
            lastObj.access = @"RO";
            [empFields addObject:lastObj];
        }
        (self.sectionFieldsMap)[kSectionEmployeeName] = empFields;
    }
    
	[super initFields];
    
    if ([self isNewReport])
    {
        self.isDirty = YES;
        //New rpt form, auto generate rpt name
        FormFieldData* rptName = [self findEditingField:@"Name"];
        if (![rptName.fieldValue length] && [rpt.reportDate length])
        {
            NSString* newRptNamePrefix = [[ReportManager sharedInstance] generateDefaultReportName:rpt.reportDate];
            rptName.fieldValue = newRptNamePrefix;
        }
    }
}

-(void) fieldUpdated:(FormFieldData *)field
{
    if ([field.iD isEqualToString:@"PolKey"])
    {
        if (field.extraDisplayInfo != nil && [field.extraDisplayInfo isKindOfClass:NSMutableDictionary.class])
        {
            NSString* fmKey = (NSString*)((NSMutableDictionary*)field.extraDisplayInfo)[@"FormKey"];
            if ((self.formKey != nil && fmKey != nil && ![fmKey isEqualToString:self.formKey]) || ![self.rpt.polKey isEqualToString:field.liKey])
            {
                [self showLoadingViewWithText:[Localizer getLocalizedText:@"Waiting"]];
                
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             field.liKey, @"POL_KEY",
                                             rpt.rptKey, @"RPT_KEY", 
                                             nil];
                [[ExSystem sharedInstance].msgControl createMsg:REPORT_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
                
                
            }
        }
    }
    // Call the server to get DynamicActions for Dynamic Field
    if ([field.isDynamicField isEqualToString:@"Y"])
    {
        [self makeDynamicActionServerCall: field];
    }

    [super fieldUpdated:field];

}
-(NSDictionary*) getComments
{
    return self.rpt.comments;
}

-(void) recalculateSections
{
    // Let's setup new sections data, before switch over
    NSMutableArray          *newSections = [[NSMutableArray alloc] initWithObjects:
                                            kSectionExceptionsName,
                                            kSectionHeaderName, 
                                            kSectionCompanyName, 
                                            kSectionEmployeeName, nil];
    
    NSMutableDictionary     *newSectionDataMap = [[NSMutableDictionary alloc] init]; 

	[self initFields];
	 
	if([rpt.exceptions count] <= 0)
	{
		for(int i = 0; i < [newSections count]; i++)
		{
			NSString *sect = newSections[i];
			
			if([sect isEqualToString:kSectionExceptionsName])
				[newSections removeObjectAtIndex:i];
		}
	}

    NSMutableArray* compFields = self.rpt.companyDisbursements;
    if (compFields == nil || [compFields count] == 0)
    {
		for(int i = 0; i < [newSections count]; i++)
		{
			NSString *sect = newSections[i];
			
			if([sect isEqualToString:kSectionCompanyName])
				[newSections removeObjectAtIndex:i];
		}
    }
    
    NSMutableArray* empFields = self.rpt.employeeDisbursements;
    if (empFields == nil || [empFields count] == 0)
    {
		for(int i = 0; i < [newSections count]; i++)
		{
			NSString *sect = newSections[i];
			
			if([sect isEqualToString:kSectionEmployeeName])
				[newSections removeObjectAtIndex:i];
        }
    }

	[newSectionDataMap removeAllObjects];

	if ([self isNewReport])
    {
		for(int i = 0; i < [newSections count]; i++)
		{
			NSString *sect = newSections[i];
			
			if([sect isEqualToString:kSectionCompanyName]|| [sect isEqualToString:kSectionEmployeeName])
				[newSections removeObjectAtIndex:i--];
		}
		
	}

	if([rpt.exceptions count] > 0)
	{
		newSectionDataMap[kSectionExceptionsName] = rpt.exceptions;
	}

    // Non-field data map for sections
    self.sections = newSections;
    self.sectionDataMap = newSectionDataMap;

// ##TODO##
//    if ([self isNewReport])
//		[self adjustViewForNewRpt];
}


-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	
	if ([msg.idKey isEqualToString:SAVE_REPORT_DATA])
	{
		SaveReportData* srData = (SaveReportData*) msg.responder;
        
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
		if (msg.errBody != nil || ![srData.reportStatus.status isEqualToString:@"SUCCESS"]) 
		{
            // default to generic message
            NSString* errMsg = [Localizer getLocalizedText:@"The server encountered an error."];
            if( nil != msg.errBody )
                errMsg = msg.errBody;
            else if( nil != srData.reportStatus.errMsg )
                errMsg = srData.reportStatus.errMsg;
            
            UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:msg.errCode
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
			[alert show];
			[self clearActionAfterSave];
			[self.tableList reloadData];
		}
		else 
		{
            if ([self isNewReport] && self.delegate != nil)
            {
                // A part of a wizard involving report creation
                [self.delegate reportCreated:srData.rpt];
                
                // Base class takes care of checks
                [ReportViewControllerBase refreshSummaryData];
            }
            else
            {
                self.isDirty = NO;
            
                NSString *dt = [DateTimeFormatter formatDateTimeMediumByDate:msg.dateOfData];
                dt = [NSString stringWithFormat:[Localizer getLocalizedText:@"Last updated"], dt];
			
                self.rpt = srData.rpt;
                [self recalculateSections];
            
                [self setupToolbarWithMessage:dt withActivity:NO];
            

                [self refreshWithUpdatedReport:self.rpt];
                [self executeActionAfterSave];
            }
		}
	}
	else if ([msg.idKey isEqualToString:REPORT_FORM_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
            [self hideLoadingView];
        }

		ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
		[self loadReport:rad.rpt];
	}
	else if ([msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA])
	{
		ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
		[self loadReport:rad.rpt];
	}
	else if (msg.parameterBag != nil && (msg.parameterBag)[@"REPORT"] != nil
             && ![msg.idKey isEqualToString:APPROVE_REPORTS_DATA]
             && ![msg.idKey isEqualToString:SUBMIT_REPORT_DATA]
             )
	{
		[self loadReport:(msg.parameterBag)[@"REPORT"]];
	}
    else if ([msg.idKey isEqualToString:GET_DYNAMIC_ACTIONS])
    {
        BOOL fRefresh = [ self updateDynamicFields:(ConditionalFieldsList *) msg.responder
                                            fields:self.allFields];
        if (fRefresh == YES)
        {
            [self.tableList reloadData];
        }
    }
}

#pragma mark Report editing Methods

-(BOOL) isNewReport
{
	return self.rpt != nil && self.rpt.rptKey == nil;
}

-(NSString*) getFormFieldsInvalidMsg
{
	return [Localizer getLocalizedText:@"REPORT_REQ_FIELDS"];
}

-(BOOL) hasCopyDownChildren
{
    return [self.rpt.rptKey length] && 
        [self.rpt hasEntry];
}

-(void) saveForm:(BOOL) cpDownToChildForms
{
	[self showWaitViewWithText:[Localizer getLocalizedText:@"Saving Report"]];
	
    if (![self isNewReport]) // For existing report, return to rpt detail
		self.actionAfterSave =kAlertViewConfirmSaveUponBack;

	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 rpt, @"REPORT", 
								 self.allFields, @"FIELDS",
								 self.role, @"ROLE_CODE",
								 cpDownToChildForms ? @"Y" : @"N", @"COPY_DOWN_TO_CHILD_FORMS", 
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];	
	NSString *sectionName = sections[section];
	
	if ([sectionName isEqual:kSectionHeaderName]|| [sectionName isEqualToString:kSectionCompanyName] || [kSectionEmployeeName isEqualToString:sectionName])
	{
		UITableViewCell *cell = [super tableView:tableView cellForRowAtIndexPath:indexPath];
		if (cell != nil)
			return cell;
	}
    
	__autoreleasing UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"BreezeData"];
	if (cell == nil) 
	{
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"BreezeData"];
	}
    
	if([sectionName isEqual:kSectionExceptionsName])
	{
		NSString *key = sections[section];
		NSMutableArray *sectionData = sectionDataMap[key];
		ExceptionData *e = sectionData[row];
		NSString* imgName = nil;
        if([e.severityLevel isEqualToString:@"ERROR"])
            imgName = @"icon_redex";
        else
            imgName = @"icon_yellowex";

        cell = [self makeExceptionCell:tableView withText:e.exceptionsStr withImage:imgName];
	}
	else 
	{
		NSString *key = sections[section];
		NSMutableArray *sectionData = sectionDataMap[key];
		NSString *val = sectionData[row];
		
		for (UIImageView *iView in [cell.contentView subviews]) 
		{
			if (iView.tag >= 900) 
			{
				[iView removeFromSuperview];
			}
		}
		
		
		UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(5, 0, 100, 40)];
		//[lbl setText:[self getLabelValue:section Row:row]];
		[lbl setBackgroundColor:[UIColor clearColor]];
		[lbl setTextAlignment:NSTextAlignmentLeft];
		[lbl setFont:[UIFont boldSystemFontOfSize:13.0f]];
		[lbl setTextColor:[UIColor blackColor]];
		[lbl setHighlightedTextColor:[UIColor whiteColor]];

		lbl.numberOfLines = 3;
		lbl.lineBreakMode = NSLineBreakByWordWrapping;
        lbl.minimumScaleFactor = 10.0/[lbl.font pointSize];
		lbl.tag = 990;
		[cell.contentView addSubview:lbl];
		
		UILabel *lblValue = [[UILabel alloc] initWithFrame:CGRectMake(110, 0, 190, 40)];

		[lblValue setText:val];
		[lblValue setBackgroundColor:[UIColor clearColor]];
		[lblValue setTextAlignment:NSTextAlignmentLeft];
		[lblValue setFont:[UIFont systemFontOfSize:13.0f]];
		[lblValue setTextColor:[UIColor blackColor]];
		[lblValue setHighlightedTextColor:[UIColor whiteColor]];
        
		lblValue.numberOfLines = 3;
		lblValue.lineBreakMode = NSLineBreakByWordWrapping;
        lbl.minimumScaleFactor = 10.0/[lbl.font pointSize];
		lblValue.tag = 990;
		[cell.contentView addSubview:lblValue];
	}
    
	return cell;
}


#pragma mark -
#pragma mark Table Delegate Methods 

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString *key = sections[section];
    if ([key isEqualToString:kSectionCompanyName]|| 
        [key isEqualToString:kSectionEmployeeName])
    {
        return [Localizer getLocalizedText:key];
    }
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	NSString *sectionName = sections[section];

    if([sectionName isEqual:kSectionExceptionsName])
	{
		NSString *key = sections[section];
		NSMutableArray *sectionData = sectionDataMap[key];
		ExceptionData *e = sectionData[row];
		
		NSString *val = e.exceptionsStr;
		CGFloat w = tableView.frame.size.width - 20 - 40;
        CGFloat height = [self getExceptionTextHeight:val withWidth:w];
		return height;
	}
    
	return [super tableView:tableView heightForRowAtIndexPath:indexPath];
}

@end
