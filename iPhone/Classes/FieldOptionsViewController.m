//
//  FieldOptionsViewController.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FieldOptionsViewController.h"
#import "TravelCustomFieldsManager.h"
#import "DownloadTravelCustomFields.h"

@interface FieldOptionsViewController() <UISearchBarDelegate, UIScrollViewDelegate>
@property (nonatomic, strong) UISearchBar *searchBar;
@property (nonatomic, strong) TravelCustomField *searchedTcf;
@end

@implementation FieldOptionsViewController
@synthesize tcf;

-(void)closeView
{ 
    if ([tcf.required boolValue] == YES && tcf.attributeValue == nil)
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PLEASE_SELECT_VALUE" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles: nil];
        [alert show];
    }
    else 
        [self.navigationController popViewControllerAnimated:YES];
}


-(void) viewDidLoad
{
    [super viewDidLoad];
    [self initializeSortedAttributes];
}

-(void) initializeSortedAttributes
{
    NSMutableArray *attributes = (NSMutableArray *)[tcf.relAttribute allObjects];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"sequence" ascending:YES];
    self.labels = [attributes sortedArrayUsingDescriptors:@[sortDescriptor]];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    if ([tcf.largeValueCount boolValue]) {
        CGRect frameRect = CGRectMake(0,0, self.view.window.frame.size.width, 44.0);
        self.searchBar = [[UISearchBar alloc] initWithFrame:frameRect];
        [tblView setTableHeaderView:self.searchBar];
        self.searchBar.delegate = self;
        self.searchBar.showsCancelButton = YES;
    }
    
    //self.selectedRowIndex = [[TravelCustomFieldsManager sharedInstance] getCustomFieldIndex:tcf forAttributeValue:tcf.selectedAttributeOptionText];
    
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[@"Back" localize] style:UIBarButtonItemStylePlain target:self action:@selector(closeView)];
}

-(void) respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:DOWNLOAD_TRAVEL_CUSTOMFIELDS] && [msg.parameterBag[@"SEARCH_TEXT"] isEqualToString:self.searchBar.text])
    {
        DownloadTravelCustomFields *searchResponder = (DownloadTravelCustomFields *)msg.responder;
        self.searchedTcf = searchResponder.field;
        [tblView reloadData];
    }
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.searchedTcf ? self.searchedTcf.attributeValues.count : labels.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{	
	NSInteger row = [indexPath row];
    
 
    static NSString *CellIdentifier = @"FieldOptionsCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) 
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];

 	cell.textLabel.font = [cell.textLabel.font fontWithSize:13.0f];    
	cell.textLabel.numberOfLines = 2;
    NSString *cellValue;
    if (!self.searchedTcf)
    {
        EntityTravelCustomFieldAttribute *tcfAttribute = (EntityTravelCustomFieldAttribute*)labels[row];
        cell.textLabel.text = [tcfAttribute.optionText lengthIgnoreWhitespace] ? tcfAttribute.optionText : tcfAttribute.value;
        cellValue = tcfAttribute.value;
    }
    else
    {
        TravelCustomFieldAttributeValue *tcfa = (TravelCustomFieldAttributeValue *)self.searchedTcf.attributeValues[row];
        cell.textLabel.text = [tcfa.optionText lengthIgnoreWhitespace] ? tcfa.optionText : tcfa.value;
        cellValue = tcfa.value;
	}
	if ([self.tcf.attributeValue isEqualToString:cellValue]) // self.tcf.attributeValue = 'user selected value'
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
	else
		cell.accessoryType = UITableViewCellAccessoryNone;	// This is necessary because the cell may have been dequeued with a checkmark
    
    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSInteger row = [indexPath row];   
    
    if (!self.searchedTcf)
    {
        EntityTravelCustomFieldAttribute *tcfAttribute = (EntityTravelCustomFieldAttribute*)labels[row];
        self.tcf.attributeValue = tcfAttribute.value;
        self.tcf.selectedAttributeOptionText = [tcfAttribute.optionText lengthIgnoreWhitespace] ? tcfAttribute.optionText : tcfAttribute.value;
    }
    else
    {
        TravelCustomFieldAttributeValue *tcfa = (TravelCustomFieldAttributeValue *)self.searchedTcf.attributeValues[row];
        self.tcf.attributeValue = tcfa.value;
        self.tcf.selectedAttributeOptionText = [tcfa.optionText lengthIgnoreWhitespace] ? tcfa.optionText : tcfa.value;
    }
    [[TravelCustomFieldsManager sharedInstance] saveIt:tcf];

    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - UISearchBar delegate

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar
{
    NSString *searchText = searchBar.text;
    [searchBar resignFirstResponder];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:/*[self getViewIDKey], @"TO_VIEW", */@"SKIP_CACHE", @"YES", searchText, @"SEARCH_TEXT", self.tcf.attributeId, @"ATTRIBUTE_ID", nil];
    [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_CUSTOMFIELDS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    [self keepSearchButtonEnabled:searchBar];
}

- (void)keepSearchButtonEnabled:(UISearchBar *)searchBar
{
    for (UIView *view in searchBar.subviews) {
        if ([ExSystem is7Plus])
        {
            for (UIView *subview in view.subviews) {
                if ([subview isKindOfClass:[UIButton class]]) {
                    ((UIButton *)subview).enabled = YES;
                    return;
                }
            }
        }
        else
        {
            if ([view isKindOfClass:[UIButton class]]) {
                ((UIButton *)view).enabled = YES;
                return;
            }
        }
    }
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar
{
    self.searchedTcf = nil;
    searchBar.text = nil;
    [searchBar resignFirstResponder];
    [self initializeSortedAttributes];
    [tblView reloadData];
}

#pragma mark - UIScrollViewDelegate
- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
    [self.searchBar resignFirstResponder];
    [self keepSearchButtonEnabled:self.searchBar];
}

@end
