//
//  ExUnitTestsVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExUnitTests.h"

@interface ExUnitTestsVC : UIViewController <UITableViewDataSource, UITableViewDelegate> {
    
    NSMutableArray          *aRows;
    NSMutableDictionary     *dictValues;
    ExUnitTests             *unitTests;
    UILabel                 *lblResults;
    UIToolbar               *tbBottom;
}

@property (nonatomic, strong) NSMutableArray          *aRows;
@property (nonatomic, strong) NSMutableDictionary     *dictValues;
@property (nonatomic, strong) ExUnitTests             *unitTests;
@property (nonatomic, strong) IBOutlet UILabel        *lblResults;
@property (nonatomic, strong) IBOutlet UIToolbar      *tbBottom;

-(IBAction) buttonClose:(id)sender;
-(BOOL) hasFails:(NSMutableArray *)a;
-(NSString *) getFails:(NSMutableArray *)a;
@end
