//
//  IgniteChatterOppPostVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterOppPostVC.h"

@interface IgniteChatterOppPostVC ()

@end

@implementation IgniteChatterOppPostVC
@synthesize tableList, dsFeed, opportunity;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.dsFeed = [[IgniteChatterOppPostFeedDS alloc] init];
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [dsFeed setSeedData:[ad managedObjectContext] withOppId:self.opportunity.opportunityId withTable:self.tableList withDelegate:nil];

    // Change title
    NSArray* navItems = self.navBar.items;
    if (navItems != nil && [navItems count] >0)
    {
        UIView* titleView = ((UINavigationItem*)[navItems objectAtIndex:0]).titleView;
        if ([titleView isKindOfClass:[UILabel class]])
        {
            UILabel *titleLabel = (UILabel*) titleView;
            titleLabel.text = [NSString stringWithFormat:@"Feed for %@", self.opportunity.opportunityName];
        }
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (void)sendChatterPost:(NSString*) text
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: text, @"TEXT", nil];
    
//    if ([self.dsFeed.feedEntryIdentifier  length])
//        [pBag setObject:self.dsFeed.feedEntryIdentifier forKey:@"FEED_ENTRY_IDENTIFIER"];
//    else {
        [pBag setObject:dsFeed.opportunityId forKey:@"RECORD_ID"];
//    }
    
    [[ExSystem sharedInstance].msgControl createMsg:CHATTER_POST_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
}

@end
