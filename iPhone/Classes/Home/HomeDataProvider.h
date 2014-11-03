//
//  HomeDataProvider.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 10/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SummaryData.h"
#import "HomeDataProviderDelegate.h"

/**
 Description : Data provider for the Home 
 This provider class handles all the MWS calls for home data and updates coredata for homescreen. 
 iPad homeVC and iPhone homeVC should gets the updates automatically from the coredata.
 
 */
@interface HomeDataProvider : NSObject <ExMsgRespondDelegate>

@property (readonly)            BOOL isTravelOnly;
@property (nonatomic, strong)   SummaryData *summaryData;
@property (weak, nonatomic)     id<HomeDataProviderDelegate>	delegate;

-(void) setupHomeData:  (BOOL)shouldSkipCache;
// More utility methods
-(void) getSummaryData: (BOOL)shouldSkipCache;
-(void) getTripsData:   (BOOL)shouldSkipCache;
-(void)preFetchExpenseData:(BOOL)shouldSkipCache;



@end
