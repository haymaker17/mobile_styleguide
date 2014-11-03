//
//  GovSelectTANumVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovSelectTANumVC.h"
#import "GovTANumbersData.h"
#import "ListFieldEditVC.h"
#import "ListItem.h"
#import "DateTimeFormatter.h"
#import "GovDutyLocationVC.h"
#import "GovTAField.h"

@interface GovSelectTANumVC (Private)
-(NSArray* ) getAuthListItems:(BOOL)isExisting;
-(GovTAField*) getAuthField;
-(GovTANumber*) findTANumber:(NSString*)taNum;
-(NSString*) getTANumName:(GovTANumber*) num;

@end

@implementation GovSelectTANumVC
@synthesize taNumbers, actionAfterCompletion, selectedTANumber, selectedNewTANum, tableList, rows, taFields;
@synthesize delegate = _delegate;

-(void) setSeedData:(NSArray*) taFlds
{
    if (taFlds == nil)
        taFlds = [GovTAField makeEmptyTAFields];
    self.taFields = taFlds;
    
    GovTAField* authFld = [self getAuthField];
    self.selectedNewTANum = [authFld isNewTANum];
    self.selectedTANumber = nil;  // Will be set once GOV_TA_NUMBERS gets filled
    
    // Go fetch TA Numbers
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:GOV_TA_NUMBERS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

}

-(GovTAField*) getAuthField
{
    return [self.taFields count]>0 ? [self.taFields objectAtIndex:0] : nil;
}

-(void) completeTaskImpl
{
    if (self.delegate != nil)
    {
        UINavigationController * navi = self.navigationController;
        [navi popViewControllerAnimated:NO];
        // Call delegate
        [self.delegate fieldUpdated:[self getAuthField]];
    }
    else
    {
        // Go to next stage
        BOOL needPerDiem = NO; //[self.actionAfterCompletion isEqualToString:@"Book Hotel"];
        [GovDutyLocationVC showDutyLocationVC:self withCompletion:self.actionAfterCompletion withFields:self.taFields withDelegate:nil withPerDiemRate:needPerDiem asRoot:NO];
    }    
}
-(void) completeTask
{
    // HACK.  The old value of .5 sec is not longer suffcient for pop/push views around
    // A good way to do this is using completion block instead of delay.
    [self performSelector:@selector(completeTaskImpl) withObject:nil afterDelay:.6];
}

#pragma mark - Msg Responder
-(void)respondToFoundData:(Msg *)msg
{    
	if ([msg.idKey isEqualToString:GOV_TA_NUMBERS])
	{
        if ([self isViewLoaded])
        {
            [self hideLoadingView];
        }
        
		GovTANumbersData *resp = (GovTANumbersData*)msg.responder;
		
		if (msg.responseCode == 200)
        {
            self.taNumbers = resp.taNumbers;
            
            self.rows = [[NSMutableArray alloc] initWithCapacity:3];
            [self.rows addObject:@"TA_AUTH_NEW"];
            if ([[self getAuthListItems:NO] count]>0)
                [self.rows addObject:@"TA_AUTH_OPEN_GROUP"];
            if ([[self getAuthListItems:YES] count]>0)
                [self.rows addObject:@"TA_AUTH_EXISTING"];

            // Set selected TANumber
            GovTAField* authFld = [self getAuthField];
            NSString *taNum = authFld.tANum;
            if (taNum != nil)
            {
                self.selectedTANumber = ![taNum length]? nil : [self findTANumber:taNum];
                authFld.tADocType = self.selectedTANumber.docType;
                authFld.tADocName = self.selectedTANumber.docName;
                authFld.tANumName = [self getTANumName: self.selectedTANumber];
                authFld.tANum = self.selectedTANumber.tANumber;
            }
        }
        
        [self.tableList reloadData];
    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.title = [@"Travel Authorization" localize];
    self.navigationController.toolbarHidden = NO;
    
    if (self.taNumbers == nil)
    {
        [self showLoadingView];
    }
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return [rows count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 54;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell"];
    if (cell == nil)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"Cell"];
    }
    
    NSString * textStr = [[rows objectAtIndex:row] localize];
    
    cell.textLabel.text = textStr;
    cell.textLabel.font = [UIFont fontWithName:@"HelveticaNeue" size:15.0f];
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;

    return cell;
}

- (GovTANumber* )findTANumber:(NSString*)taNum
{
    for (GovTANumber *num in self.taNumbers)
    {
        if ([num.tANumber isEqualToString:taNum] && [taNum length])
            return num;
    }
    return nil;
}

-(NSString*) getTANumName:(GovTANumber*) num
{
    NSString *startFormatted = [DateTimeFormatter formatDate:num.tripBeginDate Format:@"yyyy-MM-dd"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    if ([startFormatted length])
        return [NSString stringWithFormat:@"%@ (%@)", num.tANumber, startFormatted];
    else
        return num.tANumber;
}

- (NSArray* )getAuthListItems:(BOOL)isExisting
{
    NSMutableArray* result = [[NSMutableArray alloc] init];
    for (GovTANumber *num in self.taNumbers)
    {
        if (([num.tAType isEqualToString:@"Auth Without Reservations"] && isExisting) ||
            (!isExisting && ([num.tAType isEqualToString:@"Open Auth"]  || [num.tAType isEqualToString:@"Group Auth"])))
        {
            ListItem * li = [[ListItem alloc] init];
            li.liKey = num.tANumber;

            li.liName = [self getTANumName:num];
            [result addObject:li];
        }
    }
    return result;
}

- (void)showListEditor:(BOOL) isExisting
{
    NSString * label = isExisting? @"Existing Authorizations" : @"Open or Group Auths";

    FormFieldData * field = [[FormFieldData alloc] initField:@"TANumber" label:[label localize] value:nil ctrlType:@"LIST" dataType:@"INTEGER"];
    ListFieldEditVC *lvc = nil;
    lvc = [[ListFieldEditVC alloc] initWithNibName:@"ListFieldEditVC" bundle:nil];
    [lvc setSeedData:field delegate:self keysToExclude:nil];
    [lvc view];
    if([UIDevice isPad])
		lvc.modalPresentationStyle = UIModalPresentationFormSheet;
	[self.navigationController pushViewController:lvc animated:YES];
    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"MAIN_SECTION", nil];
    lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getAuthListItems:isExisting], @"MAIN_SECTION", nil];
    [lvc hideSearchBar];
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];

    NSString* rowId = [rows objectAtIndex:row];
    if ([rowId isEqualToString:@"TA_AUTH_OPEN_GROUP"])
        [self showListEditor:NO];
    else if ([rowId isEqualToString:@"TA_AUTH_EXISTING"])
        [self showListEditor:YES];
    else
    {
        self.selectedNewTANum = YES;
        self.taFields = [GovTAField makeEmptyTAFields];
        GovTAField* fld = [self getAuthField];
        fld.isNewTANum = YES;
        fld.fieldValue = [@"New Authorization" localize];

        [self completeTask];
    }
}

-(void) fieldUpdated:(FormFieldData*) field
{
    GovTAField* authFld = [self getAuthField];
    NSString *taNum = field.liKey;
    self.selectedTANumber = ![taNum length]? nil : [self findTANumber:taNum];
    authFld.tADocType = self.selectedTANumber.docType;
    authFld.tADocName = self.selectedTANumber.docName;
    authFld.tANumName = [self getTANumName: self.selectedTANumber];
    authFld.tANum = self.selectedTANumber.tANumber;
    if ([self.selectedTANumber.tAType isEqualToString:@"Group Auth"])
        authFld.useGroupAuth = YES;
    
    [self completeTask];
}

-(void) fieldCanceled:(FormFieldData *)field
{
}

-(IBAction)actionClose:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark view display
+(void) showSelectTANum:(UIViewController*)pvc withCompletion:(NSString*)booking withFields:(NSArray*) taFields withDelegate:(id<FieldEditDelegate>) del asRoot:(BOOL)isRoot
{
	GovSelectTANumVC *c = [[GovSelectTANumVC alloc] initWithNibName:@"GovSelectTANumVC" bundle:nil];
    [c setSeedData:taFields];
    c.delegate = del;
    c.actionAfterCompletion = booking;
   
    if (isRoot)
    {
        // Add Close button
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:c action:@selector(actionClose:)];
        c.navigationItem.leftBarButtonItem = btnClose;
    }
    
	[pvc.navigationController pushViewController:c animated:!isRoot];
}

@end
