//
//  SearchYodleeCardsVC.m
//  ConcurMobile
//
//  Created by yiwen on 11/3/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "SearchYodleeCardsVC.h"
#import "SearchYodleeCardData.h"
#import "AddYodleeCardVC.h"

@implementation SearchYodleeCardsVC

@synthesize searchResultsView, searchBar;
@synthesize searchResults, searchText, searchHistory;
@synthesize seg, popularCardsView, popularCards;


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle
-(void)closeView:(id)sender
{
	if([UIDevice isPad])
	{
		[self dismissViewControllerAnimated:YES completion:nil];	
	}
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if([UIDevice isPad])
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
		self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
	}
    
	self.title = [Localizer getLocalizedText:@"Select Card"];
    
    // Do any additional setup after loading the view from its nib.
    [self.seg setTitle:[Localizer getLocalizedText:@"Popular Cards"] forSegmentAtIndex:0];
    [self.seg setTitle:[Localizer getLocalizedText:@"Search"] forSegmentAtIndex:1];
    
    if(![UIDevice isPad])
	{
		searchBar.tintColor = [UIColor darkBlueConcur_iOS6];
        searchBar.alpha = 0.9f;
	} 
    else
    {
		searchBar.tintColor = [UIColor navBarTintColor_iPad];
        searchBar.alpha = 0.9f;        
    }
    
	
	self.searchHistory = [[NSMutableDictionary alloc] init];
	
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor baseBackgroundColor]];	
	[self.navigationItem setHidesBackButton:NO animated:NO];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SKIP_CACHE", @"Y", @"IS_POPULAR", nil];
	[[ExSystem sharedInstance].msgControl createMsg:SEARCH_YODLEE_CARD_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

    
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return SEARCH_YODLEE_CARDS;
}

#pragma mark - Msg Responder
-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	//respond to data that might be coming from the cache
    
	if ([msg.idKey isEqualToString:SEARCH_YODLEE_CARD_DATA])
	{
        SearchYodleeCardData* responder = (SearchYodleeCardData*) msg.responder;
        if (responder.isPopular)
        {
            self.popularCards = responder.cardList;
            [self.popularCardsView reloadData];

        }
        else
        {
            self.searchResults = responder.cardList;
            [self.searchResultsView reloadData];
        }
    }
}

#pragma mark -
#pragma mark Table view data source
- (YodleeCardProvider*) getSelectedCard:(UITableView*) tblView cellForRowAtIdexPath:(NSIndexPath*) indexPath
{
    NSUInteger row = [indexPath row];
    YodleeCardProvider *card = nil;
    if (tblView == self.searchResultsView)
        card = searchResults[row];
    else
        card = popularCards[row];

    return card;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (tableView == self.popularCardsView)
    {
        return popularCards == nil ? 0 :[popularCards count];
    }
    else
        return searchResults == nil? 0: [searchResults count];
}

- (UITableViewCell *) qSearchTableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *) indexPath
{
    // This is for quick search results 
    
	static NSString *CellIdentifier = @"Cell";
	
    __autoreleasing UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:CellIdentifier];
    }
    
    YodleeCardProvider *card = [self getSelectedCard:tableView cellForRowAtIdexPath:indexPath];

	cell.textLabel.text = card.name;
    return cell;    
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return [self qSearchTableView:tableView cellForRowAtIndexPath:indexPath];
}


#pragma mark -
#pragma mark Table view delegate
-(IBAction) showYodleeCardLoginForm:(YodleeCardProvider*)card
{	
    AddYodleeCardVC* vc = [[AddYodleeCardVC alloc] initWithNibName:@"EditFormView" bundle:nil];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:card, @"PROVIDER", nil];
    [vc setSeedData:pBag];

    [self.navigationController pushViewController:vc animated:YES];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{

    YodleeCardProvider *card = [self getSelectedCard:tableView cellForRowAtIdexPath:indexPath];
    [self showYodleeCardLoginForm:card];
}



#pragma mark -
#pragma mark Search

-(void) searchTextChanged:(NSString*) sText
{
	if (sText == nil)
		sText = @"";
	
	if ([sText isEqualToString:self.searchText])
		return;
	
	if ((self.searchHistory)[sText] != nil)
	{
		self.searchResults = (self.searchHistory)[sText];
		self.searchText = sText;
		[self.searchResultsView reloadData];
	}
	else 
	{
		[self doSearch:sText];
	}
}

-(void) doSearch:(NSString*)text
{
	if (text == nil)
		text = @"";
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:text, @"QUERY", @"YES", @"SKIP_CACHE", @"N", @"IS_POPULAR", nil];
	[[ExSystem sharedInstance].msgControl createMsg:SEARCH_YODLEE_CARD_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark -
#pragma mark UISearchBarDelegate Methods
- (void)searchBarSearchButtonClicked:(UISearchBar *)sBar
{
	[self searchTextChanged:sBar.text];
}

-(void)searchBar:(UISearchBar*)sBar textDidChange:(NSString*)sText
{
	[self searchTextChanged:sText];
}

#pragma mark -
#pragma mark UIScrollViewDelegate Methods

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
	[self.searchBar resignFirstResponder];
}


#pragma mark -
#pragma mark SegmentedControl Methods
-(IBAction)setSearchMode:(id)sender
{
    if(seg.selectedSegmentIndex == 0)
    {
        if (self.searchResultsView.hidden == NO)
        {
            [self.searchResultsView setHidden:YES];
        }
        [self.popularCardsView setHidden:NO];
    }
    else
    {
        if (self.searchResultsView.hidden == YES)
        {
            [self.searchResultsView setHidden:NO];
        }
        [self.popularCardsView setHidden:YES];
    }
    
}


@end
