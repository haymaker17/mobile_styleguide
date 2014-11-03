//
//  IgniteUserPickerVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteUserPickerVC.h"
#import "IgniteUserSearchData.h"
#import "IgniteUserSearchResult.h"

@interface IgniteUserPickerVC ()

@end

@implementation IgniteUserPickerVC

@synthesize tableList, searchString, searchResults, emptyResultSet;
@synthesize delegate = _delegate;

#pragma mark - Lifecycle
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        self.emptyResultSet = [[NSMutableArray alloc] initWithObjects:nil];
        self.searchResults = emptyResultSet;
    }
    return self;
}


#pragma mark - View Methods
- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Search Methods
- (void) searchForString:(NSString*)strSearch
{
    if (![self isViewLoaded])
        return;
    
    self.searchString = strSearch;
    self.searchResults = emptyResultSet;
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: searchString, @"SEARCH_STRING", nil];
    [[ExSystem sharedInstance].msgControl createMsg:IGNITE_SEARCH_USERS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:IGNITE_SEARCH_USERS])
    {
        if (msg.responseCode == 200)
        {
            IgniteUserSearchData *searchResultsData = (IgniteUserSearchData*) msg.responder;
            
            // Only process this message if its search string matches the current search string.
            // It might not match if multiple search messages were fired off in quick succession
            // and the responses are arriving out of order.
            //
            if ([searchResultsData.searchString isEqualToString:self.searchString])
            {
                if ([self isViewLoaded])
                {
                    self.searchResults = searchResultsData.searchResults;
                    [self.tableList reloadData];
                }
            }
        }
    }
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // If there are search results, then the number of rows is the number of search results.
    // Otherwise, there is one row to show 'No Results'
    return (self.searchResults.count > 0 ? self.searchResults.count : 1);
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"MyCell";

    // Create the cell
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
    // Determine the text and color
    NSString *text = nil;
    UIColor  *textColor = nil;
    if (self.searchResults.count == 0)
    {
        text = @"No Results"; // TODO: Localize
        textColor = [UIColor lightGrayColor];
    }
    else
    {
        IgniteUserSearchResult *searchResult = [self.searchResults objectAtIndex:indexPath.row];
        NSString *name = searchResult.name;
        text = [NSString stringWithFormat:@"%@", name];
        textColor = [UIColor blackColor];
    }
    
    // Configure the cell...
    cell.textLabel.text = text;
    cell.textLabel.textColor = textColor;

    return cell;
}

#pragma mark - Table view delegate methods
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.searchResults.count == 0)
        return;// The selected row is 'No Results'.  Ignore it.
    
    IgniteUserSearchResult *searchResult = [self.searchResults objectAtIndex:indexPath.row];
    [self.delegate userPickedSearchResult:searchResult];
}

@end
