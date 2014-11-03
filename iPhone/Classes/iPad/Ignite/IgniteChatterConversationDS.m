//
//  IgniteChatterConversationDS.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/10/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterConversationDS.h"
#import "IgniteChatterConversationCell.h"
#import "EntityChatterAbstractEntry.h"
#import "EntityChatterCommentEntry.h"
#import "EntityChatterAuthor.h"
#import "EntityChatterAttachment.h"
#import "EntityChatterFeed.h"
#import "IgniteItinDetailSocialFeedDS.h"
#import "SalesForceUserManager.h"
#import "IgniteChatterCommentView.h"

#define CHATTER_CELL_HEIGHT                 200.0
#define CHATTER_ATTACHMENT_HEIGHT            60.0
#define CHATTER_COMMENT_HEIGHT              140.0
#define CHATTER_TOTAL_LIKES_FIELD_HEIGHT     31.0

#define SECTION_FEED_ENTRY 0
#define SECTION_COMMENTS 1

@interface IgniteChatterConversationDS (private)
-(void) fetchedResults;
-(UITableViewCell *)tableView:(UITableView *)tableView configureCellForRowAtIndexPath:(NSIndexPath *)indexPath;
-(UITableViewCell *) tableView:(UITableView *)tableView configureCellForEntry:(EntityChatterAbstractEntry*)entry;
-(NSIndexPath *) fetchedResultsIndexPathFromTableIndexPath:(NSIndexPath *)tableIndexPath;
@end

@implementation IgniteChatterConversationDS

@synthesize tableList, managedObjectContext, fetchedResultsController, feedEntry;
@synthesize delegate = _delegate;

-(void) setSeedData:(NSManagedObjectContext*)con withTable:(UITableView *)tbl withDelegate:(id<IgniteChatterConversationDSDelegate>) del withFeedEntry:(EntityChatterFeedEntry*)entry
{
    self.managedObjectContext = con;
    self.feedEntry = entry;

    self.delegate = del;
    
    // The chatter feed has already been loaded, including the comments belonging to the feed.  If they had not already been loaded, then the user would not have been able to bring up the conversation view and this code would not be executing.
    [self fetchedResults];

    self.tableList = tbl;
    self.tableList.delegate = self;
    self.tableList.dataSource = self;
}


#pragma mark - NSFetchedResultsController
- (void)fetchedResults 
{
    if (fetchedResultsController == nil)
    {
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"EntityChatterCommentEntry" inManagedObjectContext:self.managedObjectContext];
        
        // Fetch entities of type EntityChatterFeedItem
        NSFetchRequest *request = [[NSFetchRequest alloc] init];
        [request setEntity:entityDescription];
        request.includesSubentities = YES;
        
        // Belonging to news feed
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"((feedEntry.identifier = %@) AND (feedEntry.feed.label = %@))", self.feedEntry.identifier, feedEntry.feed.label];
        [request setPredicate:predicate];
        
        // Order by date
        NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"createdDate" ascending:NO];
        [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
        
        self.fetchedResultsController = [[NSFetchedResultsController alloc]
                                         initWithFetchRequest:request
                                         managedObjectContext:self.managedObjectContext
                                         sectionNameKeyPath:nil
                                         cacheName:nil];
        self.fetchedResultsController.delegate = self;
        
    }
    
    NSError *error;
	if (![fetchedResultsController performFetch:&error]) {
		// Update to handle the error appropriately.
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"IgniteChatterConversationDS: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];	
	}   
}

#pragma mark - NSFetchedResultsControllerDelegate
#pragma mark - NSFetchedResultsControllerDelegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            NSLog(@"***** inserted section");
            [self.tableList insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            NSLog(@"***** deleted section");
            [self.tableList deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableList;
    
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [self tableView:tableView configureCellForRowAtIndexPath:indexPath];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList endUpdates];
}

#pragma mark - NSFetchedResultsController conversion
 

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // The feed entry section and the comment section
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == SECTION_FEED_ENTRY)
        return 1;   // There is only one feed entry (self.feedEntry)
    else
    {
        // All the comments in the fetched results are in a single section (at index zero)
        id <NSFetchedResultsSectionInfo> sectionInfo = [[fetchedResultsController sections] objectAtIndex:0];
        return [sectionInfo numberOfObjects]; // Number of comments that were fetched
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return [self tableView:tableView configureCellForRowAtIndexPath:indexPath];
}

- (UITableViewCell *)tableView:(UITableView *)tableView configureCellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    int section = [indexPath section];
    if (section == SECTION_FEED_ENTRY)
    {
        return [self tableView:tableView configureCellForEntry:self.feedEntry];
    }
    else
    {
        NSIndexPath *fetchedResultsIndexPath = [self fetchedResultsIndexPathFromTableIndexPath:indexPath];
        EntityChatterCommentEntry* comment = [fetchedResultsController objectAtIndexPath:fetchedResultsIndexPath];
        return [self tableView:tableView configureCellForEntry:comment];
    }
}

-(UITableViewCell *) tableView:(UITableView *)tableView configureCellForEntry:(EntityChatterAbstractEntry*)entry
{
    IgniteChatterConversationCell *cell = [IgniteChatterConversationCell makeCell:tableView owner:self withDelegate:self];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    cell.lblName.text = entry.author.name;
    cell.lblCompanyName.text = entry.author.companyName;
    cell.lblAge.text = [IgniteItinDetailSocialFeedDS ageFromDate:entry.createdDate];
    cell.lblText.text = entry.text;
    
    int totalLikes = [entry.totalLikes intValue];
    if (totalLikes == 0)
    {
        cell.lblLikes.hidden = YES;
        cell.imgThumb.hidden = YES;
    }
    else
    {
        cell.lblLikes.hidden = NO;
        cell.imgThumb.hidden = NO;
        cell.lblLikes.text = [NSString stringWithFormat:@"%i", totalLikes];
    }
    
    UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
    NSString *imageCacheName = [NSString stringWithFormat:@"Photo_Small_%@", entry.author.identifier];
    [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:entry.author.smallPhotoUrl RespondToImage:img IV:cell.imgView MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
    
    // Show attachment
    BOOL hasAttachment = (entry.relAttachment != nil);
    BOOL hasImagePreview = (hasAttachment && [entry.relAttachment.hasImagePreview boolValue]);
    
    // Hide the Link image and label if an image preview of the file is available.
    cell.imgLink.hidden = (!hasAttachment || hasImagePreview);
    cell.lblLink.hidden = (!hasAttachment || hasImagePreview);
    
    // Hide the File image and label if an image preview of the file is not available.
    cell.imgFile.hidden = !hasImagePreview;
    cell.lblFile.hidden = !hasImagePreview;
    
    if (hasAttachment)
    {
        if (!hasImagePreview)
        {
            // Configure the Link label
            cell.lblLink.text = entry.relAttachment.downloadUrl;
        }
        else
        {
            // Configure the File label
            cell.lblFile.text = entry.relAttachment.title;
            
            // Load the image preview of the file
            NSString *imagePath = [NSString stringWithFormat:@"%@/services/data/v25.0/chatter/files/%@/rendition", [[SalesForceUserManager sharedInstance] getInstanceUrl], entry.relAttachment.identifier];
            [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imagePath RespondToImage:img IV:cell.imgFile MVC:nil ImageCacheName:entry.relAttachment.identifier OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
        }
    }
    
    CGRect oldFrame = cell.contentView.frame;
    CGFloat heightOfCell = CHATTER_CELL_HEIGHT - (hasAttachment ? 0 : CHATTER_ATTACHMENT_HEIGHT);
    
    // Restore the original frame size, since this cell may have had comments add to it in the past.
    cell.contentView.frame = CGRectMake(oldFrame.origin.x, oldFrame.origin.y, oldFrame.size.width, heightOfCell);
    
    return cell;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    // Only the comments section has a header
    if (section == SECTION_COMMENTS)
    {
        IgniteChatterCommentView *vwCommentHeaader = [IgniteChatterCommentView makeViewWithOwner:self];
        return vwCommentHeaader;
    }
    
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    // Only the comments section has a header.
    if (section == SECTION_COMMENTS)
        return 44;
    
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityChatterAbstractEntry *entry = nil;
    
    // Get either the feed entry or the comment that is in this row
    int section = [indexPath section];
    if (section == 0)
    {
        entry = self.feedEntry;
    }
    else
    {
        NSIndexPath *fetchedResultsIndexPath = [self fetchedResultsIndexPathFromTableIndexPath:indexPath];
        EntityChatterCommentEntry* comment = [fetchedResultsController objectAtIndexPath:fetchedResultsIndexPath];
        entry = comment;
    }
    
    // Start with a full height cell
    BOOL hasAttachment = (entry.relAttachment != nil);
    CGFloat cellHeight = CHATTER_CELL_HEIGHT - (hasAttachment ? 0 : CHATTER_ATTACHMENT_HEIGHT);
    return cellHeight;
}

@end
