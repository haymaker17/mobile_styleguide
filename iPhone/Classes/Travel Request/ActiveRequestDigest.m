//
//  ActiveRequestDigest.m
//  ConcurMobile
//
//  Created by laurent mery on 26/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ActiveRequestDigest.h"
#import "ActiveRequestDigestEntryCell.h"
#import "ActiveRequestHeader.h"

#import "CTETravelRequest.h"
#import "CTETravelRequestEntry.h"
#import "CTETravelRequestComment.h"
#import "CTETravelRequestSearch.h"

#import "CCMenuMore.h"
#import "CCFormatUtilities.h"

@interface ActiveRequestDigest() <UITableViewDelegate, UITableViewDataSource>

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
@property (weak, nonatomic) IBOutlet UITextView *textViewBusinessPurpose;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteHeightViewBusinessPurpose;

@property (weak, nonatomic) IBOutlet UIView *viewComment;
@property (weak, nonatomic) IBOutlet UILabel *labelComment;
@property (weak, nonatomic) IBOutlet UITextView *textviewComment;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteHeightViewComment;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteVertSpaceViewBottom;
@property (weak, nonatomic) IBOutlet UIView *viewBottomNestedSubmitButton;
@property (weak, nonatomic) IBOutlet UIButton *buttonSubmitRequest;
@property (weak, nonatomic) IBOutlet UILabel *labelAmount;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteAmountWidth;

@property (copy, nonatomic) NSArray *entries;

@property (retain, nonatomic) CCMenuMore *menuMore;

@end


@implementation ActiveRequestDigest

@synthesize request;

static const float _heightRow = 62.0;

static NSString* const _fluryActionLoadDatas = @"load datas";
static NSString* const _fluryReportTitleRequestId = @"RequestId";

+(NSString*)viewName{
	
	return @"Requests-Digest";
}

//private
-(void)viewDidLoad{
	
	[self setInitBlocHidden:YES];
	[_textViewBusinessPurpose setHidden:YES];
	[_viewComment setHidden:YES];
	[_textviewComment setHidden:YES];
	
	[super viewDidLoad];
	[_tableViewEntries sizeToFit];
    [self updateNavigationBar];
	
	[self applyConcurStyle];
	[self applyLocalize];
	
	[self updateDatas];
}

#pragma mark - menu


-(CCMenuMore*)menuMore{
	
	if (_menuMore == nil) {
		
		NSArray *menuItems = [NSArray arrayWithObjects:
							  @{@"title":           [@"RequestHeader" localize],
                                @"segueIdentifier": @"requestHeader",
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
    
    if ([[segue identifier] isEqualToString:@"requestHeader"]) {
        
        ActiveRequestHeader *requestHeader = [segue destinationViewController];
		
        requestHeader.request = request;
		requestHeader.callerViewName = [self viewName];
		
		[CCFlurryLogs flurryLogEventOpenViewFrom:[self viewName]
									  to:[ActiveRequestHeader viewName]
							  parameters:nil];
    }
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
	[_viewBottomNestedSubmitButton setHidden:hidden];
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
	
	[_viewBusinessPurpose applyStyleWhiteBlocWithBorderTemplateOrNil:nil];
	[_labelBusinessPurpose setTextColor:[UIColor textLightTitle]];
	[_textViewBusinessPurpose setTextColor:[UIColor textDetail]];
	
	[_viewComment applyStyleWhiteBlocWithBorderTemplateOrNil:nil];
	[_labelComment setTextColor:[UIColor textLightTitle]];
	[_textviewComment setTextColor:[UIColor textDetail]];
	
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
	double heightTableViewByEntriesAndTotalRow = _tableViewEntries.rowHeight * [self tableView:nil numberOfRowsInSection:1];
	
	_constrainteHeightTableView.constant = heightTableViewByEntriesAndTotalRow;
	
	//_scrollView.contentSize = CGSizeMake(self.view.frame.size.width, 800);
	[_tableViewEntries needsUpdateConstraints];
	[_viewComment needsUpdateConstraints];
	[_viewBottomNestedSubmitButton needsUpdateConstraints];
	
	
	//********************* adjust size of the label to text content (to display background)
	[_labelAmount applyFitContentByConstrainte:_constrainteAmountWidth withMaxWidth:120 andMarge:8];
	//shift text on submit button to be centered betwen left border and right label amount
	[_buttonSubmitRequest setTitleEdgeInsets:UIEdgeInsetsMake(0, -_constrainteAmountWidth.constant, 0, 0)];
}


#pragma mark - Datas


-(void)updateDatas{
	
	[WaitViewController showWithText:@"" animated:YES];
	[CCFlurryLogs flurryLogSpinnerStartTimefrom:[ActiveRequestDigest viewName]
										 action:_fluryActionLoadDatas];

	dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
		
		//TODO: To remove when formKey will be enable on detail request
		NSString *headerFormKey = request.HeaderFormKey;
		
		// get Datas (main run loop) (we override request light provide by list by this one more rich)
		request = [[[CTETravelRequestSearch alloc] initWithStatus:@"ACTIVE"] searchRequestByID:request.RequestId];
		
		//TODO: To remove when formKey will be enable on detail request
		request.headerFormKey = headerFormKey;
		
		dispatch_sync(dispatch_get_main_queue(), ^{ //(main thread)
			
			//display bloc
			[self setInitBlocHidden:NO];
			
			//************************* static datas
			
			//convert date
			NSString *startDate = [CCFormatUtilities formatedDateMdyyyy:request.StartDate withTemplate:@"eeedM"];
			
			[_labelRequestName setText:request.Name];
			[_labelRequestStartDate setText:startDate];
			[_labelRequestStartDate applyFitContentByConstrainte:_constrainteWidthStartDate withMaxWidth:120 andMarge:8];
			[_labelRequestName updateConstraints];
			
			
			//************************* dynamic datas
			_entries = request.EntriesList;
			
			
			//convert Amount to localized
			CGFloat textViewHeightMax = 0.0;
			CGFloat margeTopTextView = 8.0;
			CGFloat heightMaxTextView = 42.0;
			
			[_labelAmount setText:[CCFormatUtilities formatAmount:request.TotalPostedAmount withCurrency:request.CurrencyCode localisedOrNil:nil]];
			
			//business Purpose
			if (request.Purpose != nil && ![request.Purpose isEqualToString:@""]) {
				
				[_textViewBusinessPurpose setText:request.Purpose];
				textViewHeightMax = [_textViewBusinessPurpose getTextViewFitToContentWithHeightMax:heightMaxTextView];
				[_textViewBusinessPurpose setHidden:NO];
				[_constrainteHeightViewBusinessPurpose setConstant:(90 - heightMaxTextView + textViewHeightMax + margeTopTextView)];
			}
			
			//Comment
			NSString *commentText;
			NSString *lastComment = [[request getLastComment] getCommentTextOnly];
			[_constrainteVertSpaceViewBottom setConstant:10];
			if (lastComment != nil && ![lastComment isEqualToString:@""]) {
				
				NSError *error = nil;
				NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:@"#[^=]*=" options:NSRegularExpressionCaseInsensitive error:&error];
				commentText = [regex stringByReplacingMatchesInString:lastComment options:0 range:NSMakeRange(0, [lastComment length]) withTemplate:@""];
				[_textviewComment setText:commentText];
				textViewHeightMax = [_textviewComment getTextViewFitToContentWithHeightMax:heightMaxTextView];
				[_viewComment setHidden:NO];
				[_textviewComment setHidden:NO];
				[_constrainteHeightViewComment setConstant:90 - heightMaxTextView + textViewHeightMax + margeTopTextView];
				[_constrainteVertSpaceViewBottom setConstant:110 - heightMaxTextView + textViewHeightMax + margeTopTextView];
			}

			
			[self updateConstraintes];
			
			//display entries on tableview
			[_tableViewEntries reloadData];
			
			[WaitViewController hideAnimated:YES withCompletionBlock:nil];
			[CCFlurryLogs flurryLogSpinnerStopTimefrom:[ActiveRequestDigest viewName]
												action:_fluryActionLoadDatas
											parameters:nil];
		});
	});
}


#pragma mark - tableView Entries


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
	
	ActiveRequestDigestEntryCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ActiveRequestDigestEntryCell"];
	
	if (indexPath.row == [_entries count]) {
		
		[cell updateCellWithTotal:request.TotalPostedAmount endCurrencyCode:request.CurrencyCode];
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


#pragma mark - memory management


- (void)dealloc{
	
	[CCFlurryLogs flurryLogEventReturnFromView:[ActiveRequestDigest viewName]
									to:self.callerViewName
							parameters:nil];
}

@end
