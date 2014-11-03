//
//  MobileTourData.m
//  ConcurMobile
//
//  Created by Sally Yan on 2/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileTourData.h"

@implementation MobileTourData

static NSString *kCollectionViewCellIdentifier = @"IntroCell";

// images for iPhone
NSString *const kImageExpense1 = @"tour_expense1_iPhone.jpg";
NSString *const kImageExpense2 = @"tour_expense2_iPhone.jpg";
NSString *const kImageTravel1 = @"tour_travel1_iPhone.jpg";

// images for iPad in portrait view
NSString *const kImageExpense1_iPad = @"tour_expense1_iPad.jpg";
NSString *const kImageExpense2_iPad = @"tour_expense2_iPad.jpg";
NSString *const kImageTravel1_iPad = @"tour_travel1_iPad.jpg";

// titles
NSString *const kExpense1Title = @"Take photos of your receipts.";
NSString *const kExpense2Title = @"Add expenses to your report.";
NSString *const kTravel1Title = @"Book Travel from anywhere.";

NSString *const kExpense2SubTitle = @"TourExpense2Subtitle";
NSString *const kTravel1SubTitle = @"And view upcoming itineraries.";

NSString *const kTourImage = @"Image";
NSString *const kTourTitle = @"Title";
NSString *const kTourSubtitle = @"SubTitle";



-(id) init
{
    self = [super init];
    
    if (self){
        if ([UIDevice isPhone]){
            // for travel and expense related
            if ([[ExSystem sharedInstance] isExpenseRelated] && [[ExSystem sharedInstance] isTravelRelated]){
                self.introScreens_iPhone = [self setImagesDataForExpenseAndTravelRelated:kImageExpense1 expense2Image:kImageExpense2 travel1Image:kImageTravel1];
            }
            // for expense related only
            else if ([[ExSystem sharedInstance] isExpenseRelated]){
                self.introScreens_iPhone = [self setImagesDataForExpenseRelatedOnly:kImageExpense1 expense2Image:kImageExpense2];
            }
            // for travel related only
            else if ([[ExSystem sharedInstance] isTravelRelated]) {
                self.introScreens_iPhone = [self setImagesDataForTravelRelatedOnly:kImageTravel1];
            }
        }
        else {      // iPad, initialize the data for both portrait and landscape views
            if ([[ExSystem sharedInstance] isExpenseRelated] && [[ExSystem sharedInstance] isTravelRelated]){
                self.introScreens_iPad = [self setImagesDataForExpenseAndTravelRelated:kImageExpense1_iPad expense2Image:kImageExpense2_iPad travel1Image:kImageTravel1_iPad];
            }
            // for expense related only
            else if ([[ExSystem sharedInstance] isExpenseRelated]){
                self.introScreens_iPad = [self setImagesDataForExpenseRelatedOnly:kImageExpense1_iPad expense2Image:kImageExpense2_iPad];
            }
            // for travel related only
            else if ([[ExSystem sharedInstance] isTravelRelated]) {
                self.introScreens_iPad = [self setImagesDataForTravelRelatedOnly:kImageTravel1_iPad];
            }
        }
    }
    return self;
}


#pragma mark - set up image array
- (NSArray*) setImagesDataForExpenseAndTravelRelated:(NSString*)expense1Img expense2Image:(NSString*)expense2Img travel1Image:(NSString*)travel1img
{
    NSArray *imgArray = @[@{kTourImage:expense1Img,kTourTitle:[kExpense1Title localize]},
                          @{kTourImage:expense2Img,kTourTitle:[kExpense2Title localize],kTourSubtitle:[kExpense2SubTitle localize]},
                          @{kTourImage:travel1img, kTourTitle:[kTravel1Title localize],kTourSubtitle:[kTravel1SubTitle localize]}
                          ];
    return imgArray;
}

- (NSArray*) setImagesDataForExpenseRelatedOnly:(NSString*)expense1Img expense2Image:(NSString*)expense2Img
{
    NSArray *imgArray = @[@{kTourImage:expense1Img,kTourTitle:[kExpense1Title localize]},
                          @{kTourImage:expense2Img,kTourTitle:[kExpense2Title localize],kTourSubtitle:[kExpense2SubTitle localize]},
                          ];
    return imgArray;
}

- (NSArray*) setImagesDataForTravelRelatedOnly:(NSString*)travel1img
{
    NSArray *imgArray = @[@{kTourImage:travel1img, kTourTitle:[kTravel1Title localize],kTourSubtitle:[kTravel1SubTitle localize]}
                          ];
    return imgArray;
}

@end
