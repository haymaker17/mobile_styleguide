//
//  IgniteChatterOppPostFeedDS.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/22/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterOppPostFeedDS.h"
#import "EntityChatterFeedEntry.h"
#import "IgniteChatterCell.h"

@implementation IgniteChatterOppPostFeedDS

@synthesize opportunityId, feedEntryIdentifier;

-(void) setSeedData:(NSManagedObjectContext *)con withOppId:(NSString *)oppId withTable:(UITableView *)tbl withDelegate:(id<IgniteItinDetailSocialFeedDelegate>) del
{
    if ([oppId length])
        self.opportunityId = oppId;
    [super setSeedData:con withTripKey:nil withTrip:nil withTable:tbl withDelegate:del];
}


- (NSString*)getFeedLabel
{
    return [NSString stringWithFormat:@"%@%@", CHATTER_OPPORTUNITY_FEED_PREFIX, self.opportunityId];
}

-(void) updateChatterFeed
{
    if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
    {
        // Debug only
        // self.opportunityId = @"006E0000004V8dOIAS";
        // Request a new feed
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
        if (![ self.opportunityId length])
            self.opportunityId = @"006E0000004V8dOIAS";
        [pBag setObject:self.opportunityId forKey:@"ITEM_ID"];
        [pBag setObject:[self getFeedLabel] forKey:@"FEED_LABEL"];
        [[ExSystem sharedInstance].msgControl createMsg:CHATTER_FEED_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else
    {
        [super updateChatterFeed];
    }
}

- (void)fetchedResults 
{
    [super fetchedResults];
    if ([[fetchedResultsController sections] count]>0)
    {
        id <NSFetchedResultsSectionInfo> sectionInfo = [[fetchedResultsController sections] objectAtIndex:0];
        if ([sectionInfo numberOfObjects] >0)
        {
            NSIndexPath* indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
            EntityChatterFeedEntry* entry = [fetchedResultsController objectAtIndexPath:indexPath];
            if (entry != nil)
            {
                self.feedEntryIdentifier = entry.identifier;
            }
        }
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [super tableView:tableView cellForRowAtIndexPath:indexPath];
    IgniteChatterCell* chatterCell = (IgniteChatterCell*) cell;
    UIColor * textColor = [UIColor colorWithRed:41.0/255.0 green:41.0/255.0 blue:41.0/255.0 alpha:1.0f];
    chatterCell.lblName.textColor = textColor;
    chatterCell.lblCompanyName.textColor = textColor;
    chatterCell.lblAge.textColor = textColor;
    chatterCell.lblText.textColor = textColor;
    chatterCell.lblLikes.textColor = textColor;
    chatterCell.lblFile.textColor = textColor;
    chatterCell.lblLink.textColor = textColor;
    chatterCell.imgReply.hidden = YES;
    chatterCell.imgReplyCount.hidden = YES;
    chatterCell.lblReplyCount.hidden = YES;
    return cell;
}   
@end
