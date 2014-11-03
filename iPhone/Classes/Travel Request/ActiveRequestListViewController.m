//
//  ActiveRequestListViewController.m
//  ConcurMobile
//
//  Created by LME on 7/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ActiveRequestListViewController.h"

#import "CTETravelRequest.h"
#import "CTETravelRequestSearch.h"

#import "ActiveRequestListCell.h"
#import "ActiveRequestDigest.h"

@interface ActiveRequestListViewController () <UITableViewDelegate, UITableViewDataSource>

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




@implementation ActiveRequestListViewController

float const _heightSection = 40.0;
float const _heightRow = 75.0;

NSString* const _fluryActionLoadDatas = @"load datas";


+(NSString*)viewName{
	
	return @"Requests-userlist";
}


-(void)viewDidLoad{
	
	[self initBlocSetHidden:YES];
	
    [super viewDidLoad];
    
	[self applyStyleConcur];
	[self applyLocalize];
	
	[self updateNavigationBar];
	[self updateDatas];
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

-(NSArray*)getDatasFromSection:(NSInteger)section{
	
	return [[_sections objectAtIndex:section] objectForKey:@"datas"];
}

-(NSString*)getTitleFromSection:(NSInteger)section{
	
	return [[_sections objectAtIndex:section] objectForKey:@"title"];
}

-(CTETravelRequest*)getRequestFromIndexPath:(NSIndexPath*)indexPath{
	
	return [[self getDatasFromSection:indexPath.section] objectAtIndex:indexPath.row];
}


-(void)updateDatas{

	[WaitViewController showWithText:@"" animated:YES];
	[CCFlurryLogs flurryLogSpinnerStartTimefrom:[self viewName]
								 action:_fluryActionLoadDatas];
	
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{

        // get Datas (main run loop)
        CTETravelRequestSearch *trList = [[CTETravelRequestSearch alloc] initWithStatus:@"ACTIVE"];
        NSArray *requests = [trList searchRequests];
		
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
			
			[CCFlurryLogs flurryLogSpinnerStopTimefrom:[ActiveRequestListViewController viewName]
										action:_fluryActionLoadDatas parameters:nil];
			
			NSDictionary *flurryReport = [[NSDictionary alloc] initWithObjects:@[
										[NSString stringWithFormat:@"%lu",(unsigned long)[[self getDatasFromSection:0] count]],
										[NSString stringWithFormat:@"%lu",(unsigned long)[[self getDatasFromSection:1] count]]
										] forKeys:@[@"Actives Count", @"Approved Count"]];
			[CCFlurryLogs flurryLogEventActionFrom:[ActiveRequestListViewController viewName]
									action:_fluryActionLoadDatas
								parameters:flurryReport];
        });
    });
}


#pragma mark - Section

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView{
    
    return [_sections count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section{
    
	return [self getTitleFromSection:section];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section{
    
    return _heightSection;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section{
    
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, tableView.bounds.size.width, _heightSection)];
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(18,0,tableView.bounds.size.width, _heightSection)];
    
    [label setText:[self getTitleFromSection:section]];
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
    
	return [[self getDatasFromSection:section] count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
    
    return _heightRow;
}

#pragma mark - Cell

// (mandatory uitableviewsource)
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{

    ActiveRequestListCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ActiveRequestListCell"];
    
   
    CTETravelRequest *request = [[self getDatasFromSection:indexPath.section] objectAtIndex:indexPath.row];
    
    [cell updateCellWithRequestDatas:request];
    
    return cell;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(ActiveRequestListCell*)activeRequestListCell{
	
	if ([[segue identifier] isEqualToString:@"digestScreen"]) {
		
		ActiveRequestDigest *digestVC = [segue destinationViewController];
		
		digestVC.request = [activeRequestListCell getRequest];
		digestVC.callerViewName = [self viewName];
		
		NSMutableDictionary *flurryReport = [[NSMutableDictionary alloc]
											 initWithObjects:@[[activeRequestListCell getRequest].RequestId]
											 forKeys:@[@"RequestId"]];
		
		[CCFlurryLogs flurryLogEventOpenViewFrom:[self viewName]
											  to:[ActiveRequestDigest viewName]
									  parameters:flurryReport];
	}}


#pragma mark - memory management

- (void)dealloc{
	
	[CCFlurryLogs flurryLogEventReturnFromView:[ActiveRequestListViewController viewName]
									to:self.callerViewName
							parameters:nil];
}

@end
