//
//  CreditCardTableViewController.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTECreditCard.h"

@interface CreditCardListTableViewController : UITableViewController

- (id)initWithCreditCards:(NSArray *)creditCards selectedCard:(CTECreditCard *)selectedCreditCard completion:(void (^)(CTECreditCard *selectedCreditCard))completion;

@end
