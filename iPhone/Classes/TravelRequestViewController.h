//
//  TravelRequestViewController.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
//#import "UnifiedWebViewController.h"

@interface TravelRequestViewController : MobileViewController <UIWebViewDelegate>
{
    UIWebView *webView;
    UIBarButtonItem *backBtn;
    UIBarButtonItem* refreshBtn;
    NSString *currentPageURL;
    
    UIControl   *backCover;
}

@property (nonatomic, strong) IBOutlet UIWebView *webView;
@property (nonatomic, strong) UIBarButtonItem *backBtn;
@property (nonatomic, strong) UIBarButtonItem* refreshBtn;
@property (nonatomic, strong) NSString *currentPageURL;

@property int currentIndex;
@property (nonatomic, strong) NSMutableDictionary *visitedPageList; // maintains browser history
@property (strong, nonatomic) NSString *viewTitle;
@property BOOL isNOT_TR;
@property (strong, nonatomic) NSURL *altURL;


-(NSString *) getViewIDKey;
-(IBAction) dismiss:(id)sender;

-(void) loadRequestAlt;
-(void) fetchAlt;

@end
