//
//  CreditCardCellData.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractTableViewCellData.h"
#import "CTECreditCard.h"

@interface ReservationCreditCardCellData : AbstractTableViewCellData

-(instancetype)initWithCTECreditCard:(NSArray *)creditCards;
-(NSArray *)getCreditCards;

@end
