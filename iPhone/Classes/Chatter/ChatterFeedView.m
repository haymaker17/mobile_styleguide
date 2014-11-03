//
//  ChatterView.m
//  ConcurMobile
//
//  Created by ernest cho on 6/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChatterFeedView.h"
#import "ChatterFeed.h"
#import "ChatterFeedPost.h"
#import "ChatterFeedTableCell.h"

#import "SalesForceUserManager.h"

@interface ChatterFeedView()

// This is the tableview that displays the chatter posts
@property (nonatomic, readwrite, strong) IBOutlet UITableView *tableView;

// This view keeps the tableview from moving over other views when in a composite view
@property (nonatomic, readwrite, strong) IBOutlet UIView *toplevelSubView;
@property (nonatomic, readwrite, strong) ChatterFeed *chatterFeed;

@property (nonatomic, readwrite, strong) UIRefreshControl *refreshControl;
@end

@implementation ChatterFeedView

// This class is meant to be used in Interface Builder.  This has some shortcomings that I didn't know about until I tried it.
// Basically you cannot have parameters at init.
//
// If you find that you need to pass a value on init, make an IBOutlet for the view and connect it.
// Then call a setup method on the view in viewDidLoad (IB views are not fully init until viewDidLoad)
- (id)initWithCoder:(NSCoder *)aDecoder {
    if ((self = [super initWithCoder:aDecoder])) {
        [[NSBundle mainBundle] loadNibNamed:@"ChatterFeedView" owner:self options:nil];
        [self addSubview:self.toplevelSubView];

        // add a refresh control for pull to refresh
        // This might be a non-standard way of using this.
        // It works as expected, but Apple's docs say this should be used with a UITableViewController and not a regular UITableView
        self.refreshControl = [[UIRefreshControl alloc] init];
        [self.refreshControl addTarget:self action:@selector(refresh:) forControlEvents:UIControlEventValueChanged];
        [self.tableView addSubview:self.refreshControl];
    }
    return self;
}

- (void)refresh:(UIRefreshControl *)refreshControl
{
    [self.chatterFeed requestSalesForceChatterFeed];
}

- (void)setItemId:(NSString *)itemId
{
    self.chatterFeed = [[ChatterFeed alloc] initWithView:self withItemId:itemId];
}

// wholesale refresh of the view
- (void)updateChatterView
{
    if ([self.refreshControl isRefreshing]) {
        [self.refreshControl endRefreshing];
    }

    [self.tableView reloadData];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (self.chatterFeed == nil) {
        return 0;
    }

    return [self.chatterFeed numberOfChatterPostsInFeed];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"ChatterFeedTableCell";
    ChatterFeedTableCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[ChatterFeedTableCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }

    ChatterFeedPost *post = [self.chatterFeed chatterPostAtIndex:indexPath.row];
    if (cell != nil && post != nil) {
        cell.author.text = post.author;
        cell.company.text = post.company;
        cell.dateString.text = post.dateString;
        cell.chatterText.text = post.text;

        [post getPortraitForImageView:cell.portrait];
    } 

    return cell;
}

// using Wannys trick for calculating tablecell height
// basically we create the cell and measure the variable area, then add it to the static area.
// it's expensive but it's not an issue until you hit a large number of rows
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"ChatterFeedTableCell";
    ChatterFeedTableCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[ChatterFeedTableCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
    ChatterFeedPost *post = [self.chatterFeed chatterPostAtIndex:indexPath.row];
    if (cell != nil && post != nil) {
        //cell.author.text = post.author;
        //cell.company.text = post.company;
        //cell.dateString.text = post.dateString;
        cell.chatterText.text = post.text;
    }
    cell.chatterText.frame = CGRectMake(0, 0, self.tableView.bounds.size.width-88, 42);

    // 58 is the height of all the elements other than the textbox
    return cell.chatterText.contentSize.height+58;
}

#pragma mark - Table view delegate

// in the initial spec there is no action taken on cell select, I should probably disable it entirely
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     */
}

@end
