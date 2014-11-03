//
//  PreSellOptionsViewController.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 09/10/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "PreSellCustomFieldSelectVC.h"

@interface PreSellCustomFieldSelectVC ()

@end

@implementation PreSellCustomFieldSelectVC

-(void)closeView
{
    if (self.tcf.optional == NO && self.tcf.userInputValue == nil)
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
    self.labels = self.tcf.attributeValues;
//    NSMutableArray *attributes = self.tcf.attributeValues;
//    
//    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"displayValue" ascending:YES];
//    self.labels = [attributes sortedArrayUsingDescriptors:@[sortDescriptor]];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[@"Back" localize] style:UIBarButtonItemStylePlain target:self action:@selector(closeView)];
}

-(void) respondToFoundData:(Msg *)msg
{
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return self.tcf.userInputValue && self.tcf.isOptional ? 2 : 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.tcf.userInputValue && self.tcf.isOptional && section == 0 ? 1 : labels.count;
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
    if (self.tcf.userInputValue && self.tcf.isOptional && indexPath.section == 0)
    {
        cell.textLabel.text = [@"Clear Current Selection" localize];
    }
    else
    {
        PreSellCustomFieldSelectOption *tcfa = (PreSellCustomFieldSelectOption *)self.tcf.attributeValues[row];
        cell.textLabel.text = [tcfa.displayValue lengthIgnoreWhitespace] ? tcfa.displayValue : tcfa.realValue;
        
        if ([self.tcf.userInputValue isEqualToString:tcfa.realValue]) // self.tcf.attributeValue = 'user selected value'
            cell.accessoryType = UITableViewCellAccessoryCheckmark;
        else
            cell.accessoryType = UITableViewCellAccessoryNone;	// This is necessary because the cell may have been dequeued with a checkmark
    }
    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	if (self.tcf.userInputValue && self.tcf.optional && indexPath.section == 0)
    {
        self.tcf.userInputValue = nil;
        self.tcf.userInputValueDisplayText = nil;
    }
    else
    {
        PreSellCustomFieldSelectOption *tcfa = (PreSellCustomFieldSelectOption *)self.tcf.attributeValues[indexPath.row];
        self.tcf.userInputValue = tcfa.realValue;
        self.tcf.userInputValueDisplayText = [tcfa.displayValue lengthIgnoreWhitespace] ? tcfa.displayValue : tcfa.realValue;
    }
    [self.navigationController popViewControllerAnimated:YES];
}

//#pragma mark - UISearchBar delegate
//
//- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar
//{
//    NSString *searchText = searchBar.text;
//    [searchBar resignFirstResponder];
//    [self keepSearchButtonEnabled:searchBar];
//    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:/*[self getViewIDKey], @"TO_VIEW", */@"SKIP_CACHE", @"YES", searchText, @"SEARCH_TEXT", self.tcf.attributeId, @"ATTRIBUTE_ID", nil];
//    [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_CUSTOMFIELDS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//}
//
//- (void)keepSearchButtonEnabled:(UISearchBar *)searchBar
//{
//    for (UIView *view in searchBar.subviews) {
//        if ([view isKindOfClass:[UIButton class]]) {
//            ((UIButton *)view).enabled = YES;
//            break;
//        }
//    }
//}
//
//- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar
//{
//    self.searchedTcf = nil;
//    searchBar.text = nil;
//    [searchBar resignFirstResponder];
//    [self initializeSortedAttributes];
//    [tblView reloadData];
//}
//
//#pragma mark - UIScrollViewDelegate
//- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
//{
//    [self.searchBar resignFirstResponder];
//}
//

@end
