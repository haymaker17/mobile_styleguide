//
//  IgniteItinDetailSocialFeedDS.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteItinDetailSocialFeedDS.h"
#import "ExSystem.h"
#import "IgniteChatterCell.h"
#import "IgniteChatterCommentView.h"
#import "EntityChatterFeedEntry.h"
#import "EntityChatterAuthor.h"
#import "EntityChatterAttachment.h"
#import "SalesForceUserManager.h"
#import "EntitySalesForceTrip.h"
#import "DateTimeFormatter.h"
#import "SalesForceTripManager.h"
#import "IgniteChatterFeedData.h"

#define CHATTER_CELL_HEIGHT                 200.0
#define CHATTER_ATTACHMENT_OR_TRIP_HEIGHT   60.0
#define CHATTER_COMMENT_HEIGHT              140.0
#define CHATTER_TOTAL_LIKES_FIELD_HEIGHT     31.0

#define COMMENT_VIEW_TAG 5

@interface IgniteItinDetailSocialFeedDS (private)
- (UITableViewCell *)tableView:(UITableView *)tableView configureCellForRowAtIndexPath:(NSIndexPath *)indexPath;
-(void) didRelyOnCachedFeed;
-(void) didReceiveFeed;
-(void) updateUI;
@end

@implementation IgniteItinDetailSocialFeedDS
@synthesize tableList, trip, tripKey, lstContact, managedObjectContext, fetchedResultsController;
@synthesize delegate = _delegate;

-(void) setSeedData:(NSManagedObjectContext *)con withTripKey:(NSString *)tKey withTrip:(EntityTrip *)t withTable:(UITableView *)tbl withDelegate:(id<IgniteItinDetailSocialFeedDelegate>) del
{
    self.managedObjectContext = con;

    [self updateChatterFeed];

    self.delegate = del;

    self.tableList = tbl;
    self.tableList.delegate = self;
    self.tableList.dataSource = self;
}

-(void) updateChatterFeed
{
    if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
    {
        // Request a new feed
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
        [[ExSystem sharedInstance].msgControl createMsg:CHATTER_FEED_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else
    {
        // Use the cached feed
        [self didRelyOnCachedFeed];
    }
}

#pragma mark - Receive methods
-(void) didRelyOnCachedFeed
{    
    [self updateUI];
}

-(void) didReceiveFeed
{
    [self updateUI];
}

-(void) updateUI
{
    [self fetchedResults];
    [self.tableList reloadData];
}

#pragma mark - Respond method
-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg];
}

-(void) respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:CHATTER_FEED_DATA])
	{
        if (msg.responseCode < 200 || msg.responseCode >= 300)
        {
            if (![[ExSystem sharedInstance] shouldErrorResponsesBeHandledSilently])
            {
                NSString *errorMessage = (msg.responseCode == 401 ? @"ChatterFeed:There was an error accessing your Salesforce account. Please go to Concur web to grant access." : @"ChatterFeed:There was an error accessing your Salesforce data.  Please try again later."); // TODO: localize

                UIAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:[Localizer getLocalizedText:@"Error"]
                                      message: errorMessage
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                                      otherButtonTitles:nil];
                [alert show];
            }
            
            [self didRelyOnCachedFeed]; // There maybe a cached feed, so keep going and we'll use that.
        }
        else
        {   
            [self didReceiveFeed];

            IgniteChatterFeedData *feedData = (IgniteChatterFeedData*)msg.responder;
            [feedData preloadPhotos];
        }
    }
}

- (NSString*)getFeedLabel
{
    return CHATTER_NEWS_FEED_LABEL;
}

#pragma mark - NSFetchedResultsController
- (void)fetchedResults 
{
    if (fetchedResultsController == nil)
    {
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"EntityChatterFeedEntry" inManagedObjectContext:self.managedObjectContext];
        
        // Fetch entities of type EntityChatterFeedItem
        NSFetchRequest *request = [[NSFetchRequest alloc] init];
        [request setEntity:entityDescription];
        
        // Belonging to news feed
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(feed.label = %@)", [self getFeedLabel]];
        [request setPredicate:predicate];
        
        // Order by date
        NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"createdDate" ascending:NO];
        [request setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
        
        self.fetchedResultsController = [[NSFetchedResultsController alloc]
                                  initWithFetchRequest:request
                                  managedObjectContext:self.managedObjectContext
                                  sectionNameKeyPath:nil
                                  cacheName:nil];
        fetchedResultsController.delegate = self;
        
    }
    
    NSError *error;
  	if (![fetchedResultsController performFetch:&error])
    {
		// Update to handle the error appropriately.
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"IgniteItinDetailSocialFeedDS: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
    }
}

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
            [self.tableList insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
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

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [[fetchedResultsController sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [[fetchedResultsController sections] objectAtIndex:section];
    return [sectionInfo numberOfObjects];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return [self tableView:tableView configureCellForRowAtIndexPath:indexPath];
}

- (UITableViewCell *)tableView:(UITableView *)tableView configureCellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityChatterFeedEntry* entry = [fetchedResultsController objectAtIndexPath:indexPath];
    
    IgniteChatterCell *cell = [IgniteChatterCell makeCell:tableView owner:self withDelegate:self];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    cell.lblName.text = entry.author.name;
    cell.lblCompanyName.text = entry.author.companyName;
    cell.lblAge.text = [IgniteItinDetailSocialFeedDS ageFromDate:entry.createdDate];
    cell.lblText.text = entry.text;
    
    // A reply is a comment.
    BOOL hasReplies = (entry.comments != nil && entry.comments.count > 0);
    cell.lblReplyCount.hidden = !hasReplies;
    cell.imgReplyCount.hidden = !hasReplies;
    
    if (hasReplies)
    {
        // Show the reply count
        cell.lblReplyCount.text = [NSString stringWithFormat:@"%i", (entry.comments == nil ? 0 : entry.comments.count)];
        
        // Calculate the actual width of the name
        CGSize nameSize = [cell.lblName.text sizeWithFont: cell.lblName.font
                                          constrainedToSize:CGSizeMake(cell.lblName.frame.size.width - 4, 18.0)
                                              lineBreakMode:NSLineBreakByTruncatingMiddle];
        
        // Move the reply count graphic just beyond the end of the name
        CGRect imgFrame = cell.imgReplyCount.frame;
        cell.imgReplyCount.frame = CGRectMake(cell.lblText.frame.origin.x + nameSize.width + 8, imgFrame.origin.y, imgFrame.size.width, imgFrame.size.height);
        
        // Move the reply count label so it aligns with the reply count graphic
        cell.lblReplyCount.frame = CGRectMake(cell.imgReplyCount.frame.origin.x + 4, cell.lblReplyCount.frame.origin.y, cell.lblReplyCount.frame.size.width, cell.lblReplyCount.frame.size.height);
        
        // Move the reply clount button so it aligns with the reply count graphic
        cell.btnReplyCount.frame = CGRectMake(cell.imgReplyCount.frame.origin.x, cell.imgReplyCount.frame.origin.y, cell.btnReplyCount.frame.size.width, cell.btnReplyCount.frame.size.width);
    }
    
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
    
    // Check for attachment
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
    
    // Check for trip
    EntitySalesForceTrip* salesforceTrip = [SalesForceTripManager lookupTripBySalesforceId:entry.parentIdentifier inContext:self.managedObjectContext];
    BOOL hasSalesforceTrip = (salesforceTrip != nil);
    
    // Hide the Trip label and button if a trip is not available
    cell.lblTrip.hidden = !hasSalesforceTrip;
    cell.lblTripDate.hidden = !hasSalesforceTrip;
    cell.btnTrip.hidden = !hasSalesforceTrip;
    
    if (hasSalesforceTrip)
    {
        cell.lblTrip.text = salesforceTrip.name;
        
        NSString *startDate = [DateTimeFormatter formatDateForTravelByDate:salesforceTrip.startDate];
        NSString *endDate = [DateTimeFormatter formatDateForTravelByDate:salesforceTrip.endDate];
        
        cell.lblTripDate.text = [NSString stringWithFormat:@"%@ - %@", startDate, endDate];
    }
    
    // Remove old comments
    for (UIView *subview in cell.contentView.subviews) 
	{
		if (subview.tag == COMMENT_VIEW_TAG) 
		{
			[subview removeFromSuperview];
		}
	}
    
    CGRect oldFrame = cell.contentView.frame;
    CGFloat heightOfCell = CHATTER_CELL_HEIGHT - (hasAttachment || hasSalesforceTrip ? 0 : CHATTER_ATTACHMENT_OR_TRIP_HEIGHT);
    
    // Restore the original frame size, since this cell may have had comments add to it in the past.
    cell.contentView.frame = CGRectMake(oldFrame.origin.x, oldFrame.origin.y, oldFrame.size.width, heightOfCell);
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityChatterFeedEntry* entry = [fetchedResultsController objectAtIndexPath:indexPath];
    
    BOOL hasAttachment = (entry.relAttachment != nil);

    EntitySalesForceTrip* salesforceTrip = [SalesForceTripManager lookupTripBySalesforceId:entry.parentIdentifier inContext:self.managedObjectContext];
    BOOL hasSalesforceTrip = (salesforceTrip != nil);
    
    CGFloat cellHeight = CHATTER_CELL_HEIGHT - (hasAttachment || hasSalesforceTrip ? 0 : CHATTER_ATTACHMENT_OR_TRIP_HEIGHT);
    
    return cellHeight;
}

#pragma mark - IgniteChatterCellDelegate methods
- (void)replyButtonPressedForCell:(IgniteChatterCell*)cell
{
    NSIndexPath* indexPath = [self.tableList indexPathForCell:cell];
    EntityChatterFeedEntry* entry = [fetchedResultsController objectAtIndexPath:indexPath];
    [self.delegate replyToChatterEntry:(EntityChatterFeedEntry*)entry];
}

- (void)conversationButtonPressedForCell:(IgniteChatterCell*)cell
{
    NSIndexPath* indexPath = [self.tableList indexPathForCell:cell];
    EntityChatterFeedEntry* entry = [fetchedResultsController objectAtIndexPath:indexPath];
    
    // The conversation will be shown in a popover.  A rectangle must be provided that specifies
    // the content to which the popover pertains.  That content is the number that the user sees
    // on top of the reply count graphic.  That number is inside the lblReplyCount label.  Therefore
    // the frame of that label is used for this rectangle.
    //
    CGRect replyFrame = cell.lblReplyCount.frame;
    const CGFloat shrinkage = 4.0;
    CGRect rect = CGRectMake(replyFrame.origin.x + shrinkage, replyFrame.origin.y + shrinkage, replyFrame.size.width - shrinkage, replyFrame.size.height - shrinkage);
    [self.delegate displayConversationForChatterEntry:entry fromRect:rect inView:cell];
}

- (void)tripButtonPressedForCell:(IgniteChatterCell*)cell
{
    // REMOVE #if and add your implementation
#if 0
    NSIndexPath* indexPath = [self.tableList indexPathForCell:cell];
    EntityChatterFeedEntry* entry = [fetchedResultsController objectAtIndexPath:indexPath];
    
    EntitySalesForceTrip* salesforceTrip = [SalesForceTripManager lookupTripBySalesforceId:entry.parentIdentifier inContext:self.managedObjectContext];
    if (salesforceTrip != nil)
    {
        // YOUR implementation goes here.  Do what you want with the trip.
    }
#endif
}

#pragma mark - Utils
+(int) daysFromDate:(NSDate*)fromDate toDate:(NSDate*)toDate
{
	NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
	NSDateComponents *components = [gregorian components:NSDayCalendarUnit fromDate:fromDate toDate:toDate options:0];
	int numDays = [components day];
	return numDays;
}

+(NSString*) ageFromDate:(NSDate*)date
{
	NSTimeInterval elapsedSeconds = [[NSDate date] timeIntervalSinceDate:date];
	
	const int secondsPerMinute = 60;
	const int secondsPerHour = (60 * secondsPerMinute);
	
	int days = [IgniteItinDetailSocialFeedDS daysFromDate:date toDate:[NSDate date]];
	int hours = elapsedSeconds	/ secondsPerHour;
	int minutes = elapsedSeconds / secondsPerMinute;
	
	NSString *elapsedTime = nil;
	if (days > 0)
		elapsedTime = [NSString stringWithFormat:@"%id", (int)days];
	else if (hours > 0)
		elapsedTime = [NSString stringWithFormat:@"%i hr", (int)hours];
	else if (minutes > 0)
		elapsedTime = [NSString stringWithFormat:@"%im", (int)minutes];
	else
		elapsedTime = @"1m";
	
	return elapsedTime;
}

#pragma mark - Lifecycle
-(void) dealloc
{
    [MsgHandler cancelAllRequestsForDelegate:self];
    
}


@end
