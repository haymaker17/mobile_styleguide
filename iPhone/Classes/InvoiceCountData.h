//
//  InvoiceCountData.h
//  ConcurMobile


#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"


@interface InvoiceCountData : MsgResponder
{
	NSString *currentElement, *path, *isInElement;

	NSString *count;

}


@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSString                  *isInElement;

@property (nonatomic, strong) NSString                  *count;


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;

-(void) flushData;


@end
