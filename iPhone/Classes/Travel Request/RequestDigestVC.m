//
//  RequestDigestVC.m
//  ConcurMobile
//
//  Created by laurent mery on 26/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestDigestVC.h"
#import "RequestDigestCell.h"
#import "RequestHeaderVC.h"
#import "RequestSegmentVC.h"

#import "CTETravelRequest.h"
#import "CTETravelRequestEntry.h"
#import "CTETravelRequestSearch.h"
#import "CTETravelRequestActions.h"
#import "CTEError.h"
#import "CTEComment.h"
@class CTEUserAction;

#import "CCMenuMore.h"
#import "CCToastMsg.h"

@interface RequestDigestVC() <UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UIView *viewHeader;
@property (weak, nonatomic) IBOutlet UILabel *labelRequestName;
@property (weak, nonatomic) IBOutlet UILabel *labelRequestStartDate;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteWidthStartDate;

@property (weak, nonatomic) IBOutlet UIView *viewToolbar;
@property (weak, nonatomic) IBOutlet UIButton *buttonAddAnotherItemToThisRequest;

@property (weak, nonatomic) IBOutlet UITableView *tableViewEntries;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteHeightTableView;

@property (weak, nonatomic) IBOutlet UIView *viewBusinessPurpose;
@property (weak, nonatomic) IBOutlet UILabel *labelBusinessPurpose;
@property (weak, nonatomic) IBOutlet UILabel *labelBusinessPurposeValue;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteHeightViewBusinessPurpose;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteHeightLabelBusinessPurposeValue;

@property (weak, nonatomic) IBOutlet UIView *viewComment;
@property (weak, nonatomic) IBOutlet UILabel *labelComment;
@property (weak, nonatomic) IBOutlet UILabel *labelCommentValue;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteHeightViewComment;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteHeightLabelCommentValue;

@property (weak, nonatomic) IBOutlet UIView *viewBottomNestedSubmitButton;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteVertSpaceTopViewBottomToViewBusinessPurpose;
@property (weak, nonatomic) IBOutlet UIButton *buttonSubmitRequest;
@property (weak, nonatomic) IBOutlet UILabel *labelAmount;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteAmountWidth;



@property (copy, nonatomic) NSArray *entries;

@property (retain, nonatomic) CCMenuMore *menuMore;

@end


@implementation RequestDigestVC {
	
	CCToastMsg *_toastMsg;
}

static const float _heightRow = 62.0;

static NSString* const _fluryActionLoadDatas = @"load datas";
static NSString* const _fluryReportTitleRequestId = @"RequestId";

+(NSString*)viewName{
	
	return @"Requests-Digest";
}

//private
-(void)viewDidLoad{
	
	_toastMsg = [[CCToastMsg alloc] initWithView:self.view];
	
	[self setInitBlocHidden:YES];
	[_labelBusinessPurposeValue setHidden:YES];
	[_viewComment setHidden:YES];
	[_labelCommentValue setHidden:YES];
	
	[super viewDidLoad];
	[_tableViewEntries sizeToFit];
    [self updateNavigationBar];
	
	[self applyConcurStyle];
	[self applyLocalize];
	
	[self updateDatas];
}

-(void)viewWillAppear:(BOOL)animated{
    
    [super viewWillAppear:animated];
    [[CTEDataTypesManager sharedManager] setDateOutputTemplate:@"eeedMMM"];
}

#pragma mark - menu


-(CCMenuMore*)menuMore{
	
	if (_menuMore == nil) {
		
		NSArray *menuItems = [NSArray arrayWithObjects:
							  @{@"title":           [@"RequestHeader" localize],
                                @"segueIdentifier": @"headerForm",
								@"imageKey":        @"iconblue_request"}
							  , nil];
		
		_menuMore = [[CCMenuMore alloc]initWithViewController:self withMenuItems:menuItems];
        [_menuMore.tableViewMenu setHidden:YES];
		[self.view addSubview:_menuMore.tableViewMenu];
	}
	return _menuMore;
}

- (IBAction)barButtonNavigationMoreTapped:(UIBarButtonItem*)sender{
	
    [[self menuMore] setHidden:![[self menuMore].tableViewMenu isHidden]];
}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender{
    
    if ([[segue identifier] isEqualToString:@"headerForm"]) {
        
        RequestHeaderVC *requestHeaderVC = [segue destinationViewController];
		
        requestHeaderVC.request = _request;
		requestHeaderVC.callerViewName = [self viewName];
		
		[CCFlurryLogs flurryLogEventOpenViewFrom:[self viewName]
									  to:[RequestHeaderVC viewName]
							  parameters:nil];
    }
	else if ([[segue identifier] isEqualToString:@"segmentForm"]) {
		
		NSIndexPath *indexPath = [_tableViewEntries indexPathForSelectedRow];
		CTETravelRequestEntry *entry  = [_entries objectAtIndex:indexPath.row];

		RequestSegmentVC *requestSegmentVC = [segue destinationViewController];
		
		requestSegmentVC.request = _request;
		requestSegmentVC.entry = entry;
		
		[CCFlurryLogs flurryLogEventOpenViewFrom:[self viewName]
											  to:[RequestSegmentVC viewName]
									  parameters:nil];
	}
}

-(BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender{
	
	RequestDigestCell *cell = sender;
	
	if (cell.isTotalRow) {
		
		return NO;
	}
	
	return YES;
}

#pragma mark - update view

/*
 * hide elements until we loading datas
 * to avoid display label name like amount, name etc...
 */
-(void)setInitBlocHidden:(BOOL)hidden{
	
	[_labelRequestName setHidden:hidden];
	[_labelRequestStartDate setHidden:hidden];
	
	[_tableViewEntries setHidden:hidden];
    [_viewBusinessPurpose setHidden:hidden];
	
	if (hidden == NO && [_request hasPermittedAction:@"submit"]) {
		
		[_viewBottomNestedSubmitButton setHidden:NO];
	}
	else {
		
		[_viewBottomNestedSubmitButton setHidden:YES];
	}
}


-(void)updateNavigationBar{
    
    UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:nil];
    [self.navigationItem setBackBarButtonItem:backButton];
}


-(void)applyConcurStyle{
	
	[self.view setBackgroundColor:[UIColor backgroundViewToHighlightWhiteSubview]];
	
	[_viewHeader setBackgroundColor:[UIColor backgroundTopHeader]];
	[_labelRequestName setTextColor:[UIColor whiteConcur]];
	[_labelRequestStartDate setTextColor:[UIColor whiteConcur]];
	
	[_viewToolbar setBackgroundColor:[UIColor backgroundToolBar]];
	[_buttonAddAnotherItemToThisRequest setTitleColor:[UIColor textToolBarButton] forState:UIControlStateNormal];
	
	//[_viewBusinessPurpose applyStyleWhiteBlocWithBorderTemplateOrNil:nil];
	[_labelBusinessPurpose setTextColor:[UIColor textLightTitle]];
	[_labelBusinessPurposeValue setTextColor:[UIColor textDetail]];
	
	//[_viewComment applyStyleWhiteBlocWithBorderTemplateOrNil:nil];
	[_labelComment setTextColor:[UIColor textLightTitle]];
	[_labelCommentValue setTextColor:[UIColor textDetail]];
	
	[_viewBottomNestedSubmitButton applyStyleWhiteBlocWithBorderTemplateOrNil:nil];
	[_buttonSubmitRequest applyStyleButtonWorkflow];
	[_labelAmount applyStyleForALabelOverButtonWorkflow];
}


-(void)applyLocalize{
	
	self.navigationItem.title = [@"Request" localize];
	
	[_buttonAddAnotherItemToThisRequest setTitle:[@"  " stringByAppendingString:[@"AddAnotherItemToThisRequest" localize]] forState:UIControlStateNormal];
	[_labelBusinessPurpose setText:[@"business_purpose" localize]];
	[_labelComment setText:[@"Comment" localize]];
	[_buttonSubmitRequest setTitle:[@"SubmitRequest" localize] forState:UIControlStateNormal];
}



-(void)updateConstraintes{
	
	//******************* adjust height tableview to its content (rows count (+- total row)
	_constrainteHeightTableView.constant = _tableViewEntries.rowHeight * [self tableView:nil numberOfRowsInSection:1];
	
	//_scrollView.contentSize = CGSizeMake(self.view.frame.size.width, 800);
	[_tableViewEntries needsUpdateConstraints];
	
	[_viewBusinessPurpose needsUpdateConstraints];
	[_labelBusinessPurposeValue needsUpdateConstraints];
	
	[_viewComment needsUpdateConstraints];
	[_labelCommentValue needsUpdateConstraints];
	
	[_viewBottomNestedSubmitButton needsUpdateConstraints];
	
	
	//********************* adjust size of the label to text content (to display background)
	[_labelAmount applyFitContentByConstrainte:_constrainteAmountWidth withMaxWidth:120 andMarge:8];
	//shift text on submit button to be centered betwen left border and right label amount
	[_buttonSubmitRequest setTitleEdgeInsets:UIEdgeInsetsMake(0, -_constrainteAmountWidth.constant, 0, 0)];
}


#pragma mark - Actions

- (IBAction)buttonSubmitTapped:(id)sender {
	
	[WaitViewController showWithText:@"" animated:YES];

	CTEUserAction *submitAction = [_request getAction:@"submit"];
	CTETravelRequestActions *cteTravelRequestActions = [[CTETravelRequestActions alloc]init];
	
	[cteTravelRequestActions action:submitAction success:^(NSString *responseObject) {
		
		if (_delegate && [_delegate respondsToSelector:@selector(digestDidDismissOnAction: andRequestData:)]){
			
			/*
			 * we send request object to refresh updated data on the previus view before pop to it
			 * TODO: _request has been provided by the responseObject
			 */
			[_delegate digestDidDismissOnAction:@"submit" andRequestData:_request];
		}
		
		//TODO: Display toast message here over the modal waiting view
		
		[WaitViewController hideAnimated:YES withCompletionBlock:nil];
		[CCFlurryLogs flurryLogEventActionFrom:[self viewName] action:@"submit" parameters:@{@"RequestID":[_request.RequestID stringValue]}];
		[self.navigationController popViewControllerAnimated:NO];
		
	} failure:^(NSString *errorMessage) {
		
		[WaitViewController hideAnimated:YES withCompletionBlock:nil];
		[_toastMsg toastWarningMessage:errorMessage];
	}];
}


#pragma mark - Datas


-(void)updateDatas{
	
	[WaitViewController showWithText:@"" animated:YES];
	[CCFlurryLogs flurryLogSpinnerStartTimefrom:[self viewName]
										 action:_fluryActionLoadDatas];

	dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
		
		// get Datas (main run loop) (we override request light provide by list by this one more rich)
		_request = [[[CTETravelRequestSearch alloc] initWithStatus:@"ACTIVE"] searchRequestByID:[_request.RequestID stringValue]];
		
		dispatch_sync(dispatch_get_main_queue(), ^{ //(main thread)
			
			//display bloc
			[self setInitBlocHidden:NO];
			
			//************************* static datas
			
			[_labelRequestName setText:[_request.Name stringValue]];
			
			[_labelRequestStartDate setText:[_request.StartDate stringValue]];
			
			[_labelRequestStartDate applyFitContentByConstrainte:_constrainteWidthStartDate withMaxWidth:120 andMarge:8];
			[_labelRequestName updateConstraints];
			
			
			//************************* dynamic datas
			_entries = _request.Entries;
			
			
			//convert Amount to localized
			CGFloat labelHeight = 0.0;
			CGFloat margeBottomLabel = 10.0;
			CGFloat heightMaxLabel = 10000.0; //no limit
			
			[_labelAmount setText:[_request.TotalPostedAmount stringValue]];
			
			//business Purpose
			if (![_request.Purpose isEmpty]) {
				[_labelBusinessPurposeValue setText:[_request.Purpose stringValue]];
				labelHeight = [_labelBusinessPurposeValue getTextViewFitToContentWithHeightMax:heightMaxLabel];
				[_labelBusinessPurposeValue setHidden:NO];
				[_constrainteHeightViewBusinessPurpose setConstant:_labelBusinessPurposeValue.frame.origin.y + labelHeight + margeBottomLabel];
				[_constrainteHeightLabelBusinessPurposeValue setConstant:labelHeight];
			}
			else {
				
				[_constrainteHeightViewBusinessPurpose setConstant:_labelBusinessPurposeValue.frame.origin.y + margeBottomLabel];
			}
			
			//Comment
            CTEComment *objComment = [_request getLastComment];
			CTEDataTypes *lastComment = objComment.CommentLight;
			if (![lastComment isEmpty]) {
				
				[_labelCommentValue setText:[lastComment stringValue]];
				labelHeight = [_labelCommentValue getTextViewFitToContentWithHeightMax:heightMaxLabel];
				[_viewComment setHidden:NO];
				[_labelCommentValue setHidden:NO];
				[_constrainteHeightViewComment setConstant:_labelCommentValue.frame.origin.y + labelHeight + margeBottomLabel];
				[_constrainteHeightLabelCommentValue setConstant:labelHeight];
				[_constrainteVertSpaceTopViewBottomToViewBusinessPurpose setConstant:_labelCommentValue.frame.origin.y + labelHeight + margeBottomLabel + 20.0];
			}
			else {
				
				[_constrainteHeightViewComment setConstant:0];
			}

			[self updateConstraintes];
			
			//display entries on tableview
			[_tableViewEntries reloadData];
			
			[WaitViewController hideAnimated:YES withCompletionBlock:nil];
			[CCFlurryLogs flurryLogSpinnerStopTimefrom:[self viewName]
												action:_fluryActionLoadDatas
											parameters:nil];
		});
	});
}


#pragma mark - tableView (segments)


//(protocol mandatory : uitableviewsource)
-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
	
	NSInteger rows = [_entries count];
	
	if (rows > 0) {
		
		rows++; // add total row if one entry
	}

	return rows;
}

// (protocol mandatory : uitableviewsource)
-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	RequestDigestCell *cell = [tableView dequeueReusableCellWithIdentifier:@"RequestDigestCell"];
	
	if (indexPath.row == [_entries count]) {
		
		[cell updateCellWithTotal:[_request.TotalPostedAmount stringValue]];
	}
	else {
		
		CTETravelRequestEntry *entry = [_entries objectAtIndex:indexPath.row];
		[cell updateCellWithEntry:entry];
	}
	
	return cell;
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	return _heightRow;
}

//to remove inset margin (bottom separator
-(void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath{

	if (indexPath.row == [_entries count]) { //remove only for total row

		if ([tableView respondsToSelector:@selector(setSeparatorInset:)]) {
			[tableView setSeparatorInset:UIEdgeInsetsZero];
		}
		
		if ([tableView respondsToSelector:@selector(setLayoutMargins:)]) {
			[tableView setLayoutMargins:UIEdgeInsetsZero];
		}
		
		if ([cell respondsToSelector:@selector(setLayoutMargins:)]) {
			[cell setLayoutMargins:UIEdgeInsetsZero];
		}
	}
}

#pragma mark - memory management


- (void)dealloc{
	
	_delegate = nil;
	[CCFlurryLogs flurryLogEventReturnFromView:[self viewName]
									to:self.callerViewName
							parameters:nil];
}

@end
