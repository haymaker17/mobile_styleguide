//
//  ChatterFeed.m
//  ConcurMobile
//
//  Created by ernest cho on 6/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChatterFeed.h"
#import "ChatterFeedPost.h"
#import "ChatterPostLookup.h"
#import "ExSystem.h"

#import "SalesForceUserManager.h"
// old parser used to populate core data.  This is from the Ignite demo.
#import "IgniteChatterFeedData.h"
// lets me read from core data.  This is from the Ignite demo.
#import "EntityChatterFeedEntry.h"
#import "EntityChatterAuthor.h"

#import "ChatterCommentsMsg.h"

@interface ChatterFeed()
@property (nonatomic, readwrite, strong) NSManagedObjectContext *context;

// spec change!  instead of showing all posts.  we show one post and it's comments
@property (nonatomic, readwrite, strong) EntityChatterFeedEntry *firstPost;
@property (nonatomic, readwrite, strong) NSArray *chatterPostComments;

@property (nonatomic, readwrite, weak) ChatterFeedView *view;
@property (nonatomic, readwrite, copy) NSString *itemId;
@end

@implementation ChatterFeed

- (id)initWithView:(ChatterFeedView *)view withItemId:(NSString *)itemId
{
    self = [super init];
    if (self) {
        self.view = view;
        self.itemId = itemId;
        self.context = [[ExSystem sharedInstance] context];

        // show cached data
        [self getChatterPostComments];

        // request fresh data
        [self requestSalesForceChatterFeed];
    }
    return self;
}

// this is called after a fetch request.
- (void)updateAfterDataFetch
{    
    [self getChatterPostComments];

    // crap. the post was deleted on the server side.  remove it from the list and inform user.
    // TODO: open up the post screen after dimissal
    if (self.firstPost == nil) {
        ChatterPostLookup *lookup = [[ChatterPostLookup alloc] init];
        [lookup removeTrip:self.itemId];

        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:@"Your post was deleted on the server!"
                              message:nil
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        [alert show];
    }

    if (self.view != nil) {
        [self.view updateChatterView];
    }
}

- (void)getChatterPostComments
{
    self.firstPost = nil;
    self.chatterPostComments = nil;

    NSError *error;
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityChatterFeedEntry" inManagedObjectContext:self.context];

    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    // we only want results with the same item id
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(identifier = %@)", self.itemId];
    [fetchRequest setPredicate:predicate];
    [fetchRequest setEntity:entity];

    NSArray *temp = [self.context executeFetchRequest:fetchRequest error:&error];
    if (temp != nil && temp.count == 1) {
        self.firstPost = (EntityChatterFeedEntry *)temp[0];

        // sort the comments by date
        NSSortDescriptor *dateSort = [[NSSortDescriptor alloc] initWithKey:@"createdDate" ascending:YES];
        NSArray *descriptors = [NSArray arrayWithObject:dateSort];
        self.chatterPostComments = [[self.firstPost.comments allObjects] sortedArrayUsingDescriptors:descriptors];
    }
}

- (int)numberOfChatterPostsInFeed
{
    int numPosts = 0;
    if (self.firstPost != nil) {
        numPosts = 1;
    }

    if (self.chatterPostComments != nil) {
        numPosts = self.chatterPostComments.count + numPosts;
    }

    return numPosts;
}

// The only reason I hide the EntityChatterFeedEntry from client classes is that I dont want them to know this is Core Data backed by a network call.
- (ChatterFeedPost *)chatterPostAtIndex:(int)index
{
    if (index == 0) {
        return [[ChatterFeedPost alloc] initWithEntityChatterFeedEntry:self.firstPost];
    } else {
        return [[ChatterFeedPost alloc] initWithEntityChatterCommentEntry:self.chatterPostComments[index-1] withEntityChatterFeedEntry:self.firstPost];
    }
}

#pragma mark - Network handling
// request chatter feed.  this only returns the first 3 comments.  WTF
- (void)requestSalesForceChatterFeed
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    [[ExSystem sharedInstance].msgControl createMsg:CHATTER_FEED_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

// request comments.  This gets the first 25 comments.
- (void)requestCommentsForChatterFeed:(NSString *)itemId
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    [pBag setValue:itemId forKey:@"ITEM_ID"];
    [[ExSystem sharedInstance].msgControl createMsg:CHATTER_COMMENTS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg];
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:CHATTER_FEED_DATA]) {
        // This populates CoreData with a Chatter Feed.  Reusing Charlotte's Ignite demo code.
        IgniteChatterFeedData *feedData = (IgniteChatterFeedData *)msg.responder;
        [feedData preloadPhotos];

        // request the comments
        [self requestCommentsForChatterFeed:self.itemId];
    }

    if ([msg.idKey isEqualToString:CHATTER_COMMENTS_DATA]) {
        // update view after getting comments
        [self updateAfterDataFetch];
    }
}

@end
