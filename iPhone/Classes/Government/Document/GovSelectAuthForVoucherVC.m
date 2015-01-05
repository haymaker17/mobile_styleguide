//
//  GovSelectAuthForVoucherVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/19/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovSelectAuthForVoucherVC.h"
#import "ConcurMobileAppDelegate.h"
#import "GovCreateVoucherFromAuthData.h"

#import "EntityGovDocument.h"
#import "GovDocDetailVC.h"
#import "GovDocDetailVC_iPad.h"

@interface GovSelectAuthForVoucherVC ()

@end

@implementation GovSelectAuthForVoucherVC

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [@"Select Authorization" localize];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(BOOL) canAdd
{
    return FALSE;
}

-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
    if ([msg.idKey isEqualToString:GOV_CREATE_VOUCHER_FROM_AUTH])
	{
        if ([self isViewLoaded])
            [self hideLoadingView];
        
        GovCreateVoucherFromAuthData *data = (GovCreateVoucherFromAuthData*) msg.responder;
        NSString * errMsg = msg.errBody;
        if (![errMsg lengthIgnoreWhitespace] && data.status != nil)
        {
            errMsg = data.status.errMsg;
        }
        
        if (errMsg != nil)
        {
            NSDictionary *param = @{@"Create From Authorization": @"Fail"};
            [Flurry logEvent:@"Voucher: Create From Authorization" withParameters:param];
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Unable to create voucher"]
                                  message:errMsg
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            
            [alert show];
            
            [self.tableView setUserInteractionEnabled:YES];
        }
        else
        {
            // Need to remove the document from GovSelectAuthForVoucher
            // request document list again (see GovDocumentListVC.m)
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"REFRESHING", nil];
            NSInteger vcIdx = [self.navigationController.viewControllers count] - 1;
            GovDocumentListVC *presentingVC = (GovDocumentListVC *)[self.navigationController.viewControllers objectAtIndex:vcIdx - 1];
            [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENTS_AUTH_FOR_VCH CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:presentingVC];
            
            // MOB-12249
            if ([UIDevice isPad])
            {
                [self dismissViewControllerAnimated:NO completion:nil];
            	//MOB-13165 to solve coredata used cross multiple thread issue
            	// fetch "EntityGovDocument *newlyCreatedDoc" by docName and DocType in coredata
            	// OR,
            	// Use GovCreateVoucherFromAuthData data directly without fetch from coredata
            
                [GovDocDetailVC_iPad showDocDetailWithTraveler:data.travid withDocName:data.returnDocName withDocType:data.returnDocType];
            }
            else
            {
                UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
                [homeVC.navigationController popViewControllerAnimated:NO];
                [GovDocDetailVC showDocDetail:homeVC withTraveler:data.travid withDocName:data.returnDocName withDocType:data.returnDocType withGtmDocType:data.gtmDocType];
            }
            NSDictionary *param = @{@"Create From Authorization": @"Success"};
            [Flurry logEvent:@"Voucher: Create From Authorization" withParameters:param];
        }
    }
    else if ([msg.idKey isEqualToString:GOV_DOCUMENTS_AUTH_FOR_VCH])
    {
        [self resetData];
        if ([@"YES" isEqualToString: [msg.parameterBag objectForKey:@"REFRESHING"]])
            [self doneRefreshing];
    }
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self.tableView setUserInteractionEnabled:NO];
    
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityGovDocument *doc = (EntityGovDocument *)managedObject;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 doc.travelerId, @"TRAVELER_ID",
								 doc.docType, @"DOC_TYPE",
								 doc.docName, @"DOC_NAME",
								 nil];
    // Send msg to create VCH
	[[ExSystem sharedInstance].msgControl createMsg:GOV_CREATE_VOUCHER_FROM_AUTH CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


#pragma mark - Fetched results controller
- (void)fetchedResults
{
    if (self.fetchedResultsController == nil)
    {
        NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityGovDocument" inManagedObjectContext:self.managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"docName" ascending:YES];
        NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"tripBeginDate" ascending:YES];
        
        [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sort, sort2, nil]];
        
        NSPredicate *pred = [NSPredicate predicateWithFormat:@"(authForVch = %@)", [NSNumber numberWithBool:YES]];
        [fetchRequest setPredicate:pred];
        
        NSFetchedResultsController *theFetchedResultsController =
        [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                            managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil
                                                       cacheName:nil] ;
        self.fetchedResultsController = theFetchedResultsController;
        self.fetchedResultsController.delegate = self;
        
        
    }
    NSError *error;
	if (![self.fetchedResultsController performFetch:&error]) {
		// Update to handle the error appropriately.
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"GovSelectAuthForVoucherVC: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
	}
}

#pragma NoDataMasterViewDelegate method
- (NSString*) titleForNoDataView
{
    return @"No Authorization";
}

-(NSString*) imageForNoDataView
{
    return @"";
}

#pragma mark -
#pragma mark GovDocumentListVC methods
-(BOOL)enablePullDownRefresh
{
    // Refresh at this level messup coredata for Document list.
    // Temerperarly disabled.
    return false;
}

- (BOOL)refreshView:(UIRefreshControl *)refresh
{
    [self showLoadingView];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"REFRESHING", nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENTS_AUTH_FOR_VCH CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

    return NO;
}

@end
