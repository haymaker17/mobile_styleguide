//
//  CardTransaction.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CardTransaction.h"
#import "SmartExpenseManager.h"

@implementation CardTransaction

@synthesize smartExpenseMeKey;

-(NSString*)meKeyImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense meKey];
	}
	return [super meKeyImpl];
}

-(void)setMeKeyImpl:(NSString *)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense setMeKey:newValue];
	}
	return [super setMeKeyImpl:newValue];
}

-(NSString*)expKeyImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense expKey];
	}
	return [super expKeyImpl];
}

-(void)setExpKeyImpl:(NSString *)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense setExpKey:newValue];
	}
	return [super setExpKeyImpl:newValue];
}

-(NSString*)expNameImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense expName];
	}
	return [super expNameImpl];
}

-(void)setExpNameImpl:(NSString *)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		[super setExpNameImpl:newValue]; // MOB-2553 Needs to be set on this object as well as the paired mobile expense, so if they're split, this object will have it as well.
		return [pairedMobileExpense setExpName:newValue];
	}
	return [super setExpNameImpl:newValue];
}

-(NSString*)commentImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense comment];
	}
	return [super commentImpl];
}

-(void)setCommentImpl:(NSString *)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense setComment:newValue];
	}
	return [super setCommentImpl:newValue];
}

-(NSString*)hasReceiptImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense hasReceipt];
	}
	return [super hasReceiptImpl];
}

-(void)setHasReceiptImpl:(NSString *)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense setHasReceipt:newValue];
	}
	return [super setHasReceiptImpl:newValue];
}

-(UIImage*)receiptImageImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense receiptImageImpl];
	}
	return [super receiptImageImpl];
}

-(void)setReceiptImageImpl:(UIImage *)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense setReceiptImage:newValue];
	}
	return [super setReceiptImageImpl:newValue];
}

-(BOOL)updatedImageImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense updatedImage];
	}
	return [super updatedImageImpl];
}

-(void)setUpdatedImageImpl:(BOOL)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense setUpdatedImage:newValue];
	}
	return [super setUpdatedImageImpl:newValue];
}

-(NSData*)receiptDataImpl
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense receiptData];
	}
	return [super receiptDataImpl];
}

-(void)setReceiptDataImpl:(NSData *)newValue
{
	OOPEntry *pairedMobileExpense = [[SmartExpenseManager getInstance] getMobileExpensePairedWithCardTransaction:self];
	if (pairedMobileExpense != nil)
	{
		return [pairedMobileExpense setReceiptData:newValue];
	}
	return [super setReceiptDataImpl:newValue];
}

#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
	[super encodeWithCoder:coder];
    [coder encodeObject:smartExpenseMeKey	forKey:@"smartExpenseMeKey"];
}

- (id)initWithCoder:(NSCoder *)coder {
    self = [super initWithCoder:coder];
    self.smartExpenseMeKey = [coder decodeObjectForKey:@"smartExpenseMeKey"];
	return self;
}


@end
