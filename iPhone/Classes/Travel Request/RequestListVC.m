//
//  RequestListVC.m
//  ConcurMobile
//
//  Created by LME on 7/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestListVC.h"

#import "CTETravelRequest.h"
#import "CTETravelRequestSearch.h"

#import "RequestListCell.h"
#import "RequestDigestVC.h"

#import "CCToastMsg.h"


@interface RequestListVC () <UITableViewDelegate, UITableViewDataSource, RequestDigestDelegate>

//interface
@property (weak, nonatomic) IBOutlet UIView *toolbar;
@property (weak, nonatomic) IBOutlet UIButton *toolbarAddButton;

//empty mode
@property (weak, nonatomic) IBOutlet UIImageView *imgArrowToShowAddActionWhenNoRequest;
@property (weak, nonatomic) IBOutlet UILabel *labelStartYourRequest;
@property (weak, nonatomic) IBOutlet UILabel *labelStartYourRequestDetail;

//tableview component
@property (weak, nonatomic) IBOutlet UITableView *tableViewRequestList;

//datas
@property (copy, nonatomic) NSArray *sections;


@end




@implementation RequestListVC {
	
	CCToastMsg *_toastMsg;
	BOOL _showMessageSubmit;
}

float const _heightSection = 40.0;
float const _heightRow = 75.0;

NSString* const _fluryActionLoadDatas = @"load datas";


+(NSString*)viewName{
	
	return @"Requests-userlist";
}


-(void)viewDidLoad{

	_toastMsg = [[CCToastMsg alloc] initWithView:self.view];
	
	[self initBlocSetHidden:YES];
	
    [super viewDidLoad];
    
	[self applyStyleConcur];
	[self applyLocalize];
	
	[self updateNavigationBar];
    [self updateDatas];
}

-(void) viewWillAppear:(BOOL)animated{
	
	[super viewWillAppear:animated];
	
	if (_showMessageSubmit == YES) {
		
        [self updateDatas];
		[_toastMsg toastWarningMessage:[@"The Request has been successfully submitted" localize]];
		_showMessageSubmit = NO;
	}
    
    [[CTEDataTypesManager sharedManager] setDateOutputTemplate:@"eeedMMM"];
}




#pragma mark - update view


/*
 * show/hide elements need to be hidden during dataloader
 */
-(void)initBlocSetHidden:(BOOL)hidden{
	
	[_imgArrowToShowAddActionWhenNoRequest setHidden:hidden];
	[_labelStartYourRequest setHidden:hidden];
	[_labelStartYourRequestDetail setHidden:hidden];
	[_tableViewRequestList setHidden:hidden];
}

/*
 * empty mode display some graphic elements instead tableview when tableview is empty
 */
-(void)emptyModeSetHidden:(BOOL)hidden{
	
	_imgArrowToShowAddActionWhenNoRequest.hidden = hidden;
	_labelStartYourRequest.hidden = hidden;
	_labelStartYourRequestDetail.hidden = hidden;
	
	_tableViewRequestList.hidden = !hidden;
}


/*
 * apply Concur's style
 * On Interface builder we keep default color to find out quickly undefined color
 */
-(void)applyStyleConcur{
    
    [_labelStartYourRequest setTextColor:[UIColor textSubTitle]];
    [_labelStartYourRequestDetail setTextColor:[UIColor textDetail]];
    
    [_toolbar setBackgroundColor:[UIColor backgroundToolBar]];
    [_toolbarAddButton setTitleColor:[UIColor textToolBarButton] forState:UIControlStateNormal];
}

/*
 * we regroup in this method all localized actions
 */
-(void)applyLocalize{
	
	//button "+ Start a new Request"
	[_toolbarAddButton setTitle: [@"  "
								  stringByAppendingString:[@"StartARequest" localize]]
					   forState:UIControlStateNormal];
	
	//text bellow arrow on empty mode
	[_labelStartYourRequest setText:[@"StartYourRequest" localize]];
	[_labelStartYourRequestDetail setText:[@"GetApprovedForAirHotelAndMore" localize]];
	
	self.navigationItem.title = [@"Requests" localize];
}

/*
 * keep in mind that we defined here navigation bar output for next pushed view
 */
-(void)updateNavigationBar{
	
	UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:nil];
	[self.navigationItem setBackBarButtonItem:backButton];
}

#pragma mark - Datas

-(NSArray*)datasFromSection:(NSInteger)section{
	
	return [[_sections objectAtIndex:section] objectForKey:@"datas"];
}

-(NSString*)titleFromSection:(NSInteger)section{
	
	return [[_sections objectAtIndex:section] objectForKey:@"title"];
}

-(CTETravelRequest*)requestFromIndexPath:(NSIndexPath*)indexPath{
	
	return [[self datasFromSection:indexPath.section] objectAtIndex:indexPath.row];
}

-(void)setRequest:(CTETravelRequest*)request atIndexPath:(NSIndexPath*)indexPath{
	
	
	NSArray *datasForSection = [self datasFromSection:indexPath.section];
	//[datasForSection setValue:request forKey:[NSString stringWithFormat:@"%ld", (long)indexPath.row]];
}

-(void)updateDatas{

	[WaitViewController showWithText:@"" animated:YES];
	[CCFlurryLogs flurryLogSpinnerStartTimefrom:[self viewName]
								 action:_fluryActionLoadDatas];
	
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{

        // get Datas (main run loop)
        CTETravelRequestSearch *trList = [[CTETravelRequestSearch alloc] initWithStatus:@"ACTIVE"];
        NSArray *requests = [trList searchRequests];

        /* Used only for test perso (lme)*/
        CTETravelRequest *req;
        for (req in requests){
            
            if ([[req.Name stringValue] isEqualToString:@"test full cellphone"]){
                
                break;
            }
        }
        requests = @[req];
        // */
        
        dispatch_sync(dispatch_get_main_queue(), ^{
            
            /*=== init sections === (main thread)*/
			
            //Active Requests (split -> sections)
            NSDictionary *sectionActiveRequests = [NSDictionary
												   dictionaryWithObjects:@[[@"ACTIVEREQUESTS" localize], [requests filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"_ApprovalStatusCode!='Q_APPR'"]]]
												   forKeys:@[@"title", @"datas"]];
            
            //Approved Requests
            NSDictionary *sectionApprovedRequests = [NSDictionary dictionaryWithObjects:@[[@"APPROVEDREQUESTS" localize], [requests filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"_ApprovalStatusCode='Q_APPR'"]]] forKeys:@[@"title", @"datas"]];
            
            _sections = @[sectionActiveRequests, sectionApprovedRequests];
            
            
            //Manage empty result
            if (requests.count == 0) {
                
                // hide/show all item relative to empty mode view
                [self emptyModeSetHidden:false];
            }
            else {
                
                // hide/show all item relative to empty mode view
                [self emptyModeSetHidden:true];
				
				//display datas
                [_tableViewRequestList reloadData];
            }
            
			[WaitViewController hideAnimated:YES withCompletionBlock:nil];
			
			[CCFlurryLogs flurryLogSpinnerStopTimefrom:[self viewName]
										action:_fluryActionLoadDatas parameters:nil];
			
			NSDictionary *flurryReport = [[NSDictionary alloc] initWithObjects:@[
										[NSString stringWithFormat:@"%lu",(unsigned long)[[self datasFromSection:0] count]],
										[NSString stringWithFormat:@"%lu",(unsigned long)[[self datasFromSection:1] count]]
										] forKeys:@[@"Actives Count", @"Approved Count"]];
			[CCFlurryLogs flurryLogEventActionFrom:[self viewName]
									action:_fluryActionLoadDatas
								parameters:flurryReport];
        });
    });
}

//@protocol RequestDigestDelegate
-(void)digestDidDismissOnAction:(NSString *)action andRequestData:(CTETravelRequest *)request{
	

	if ([@"submit" isEqualToString:action]){
		
		_showMessageSubmit = YES;
		
		/*
		 * list still selected after a pop from a child view
		 and this method is always called in this context
		 */
		NSIndexPath *indexPath = [_tableViewRequestList indexPathForSelectedRow];

		//update datas
		[self setRequest:request atIndexPath:indexPath];

		//refresh only the request row
		[_tableViewRequestList reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:NO];
	}
}


#pragma mark - Section

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView{
    
    return [_sections count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section{
    
	return [self titleFromSection:section];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section{
    
    return _heightSection;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section{
    
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, tableView.bounds.size.width, _heightSection)];
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(18,0,tableView.bounds.size.width, _heightSection)];
    
    [label setText:[self titleFromSection:section]];
    [label setTextColor:[UIColor blackColor]];
    [label setFont:[UIFont fontWithName:@"HelveticaNeue" size:12]];
    [headerView setBackgroundColor:[UIColor backgroundHeaderSection]];
    
    [headerView addSubview:label];
    
    return headerView;
}

// remove the footer (0 doesn't work)
- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section{
	
	return 0.1;
}


#pragma mark - Row

//(mandatory uitableviewsource)
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    
	return [[self datasFromSection:section] count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
    
    return _heightRow;
}

#pragma mark - Cell

// (mandatory uitableviewsource)
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{

    RequestListCell *cell = [tableView dequeueReusableCellWithIdentifier:@"RequestListCell"];
    CTETravelRequest *request = [[self datasFromSection:indexPath.section] objectAtIndex:indexPath.row];
    
    [cell updateCellWithRequestDatas:request];
    
    return cell;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(RequestListCell*)RequestListCell{
	
	if ([[segue identifier] isEqualToString:@"digestScreen"]) {
		
		RequestDigestVC *digestVC = [segue destinationViewController];
		CTETravelRequest *request = [RequestListCell getRequest];
		
		digestVC.request = request;
		digestVC.callerViewName = [self viewName];
		digestVC.delegate = self;
		
		NSMutableDictionary *flurryReport = [[NSMutableDictionary alloc]
											 initWithObjects:@[[request.RequestID stringValue]]
											 forKeys:@[@"RequestID"]];
		
		[CCFlurryLogs flurryLogEventOpenViewFrom:[self viewName]
											  to:[RequestDigestVC viewName]
									  parameters:flurryReport];
	}}


#pragma mark - memory management

- (void)dealloc{
	
	[CCFlurryLogs flurryLogEventReturnFromView:[RequestListVC viewName]
									to:self.callerViewName
							parameters:nil];
}

@end
